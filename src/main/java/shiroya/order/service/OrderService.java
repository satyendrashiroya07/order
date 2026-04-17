package shiroya.order.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import shiroya.order.entity.Order;
import shiroya.order.exception.InsufficientStockException;
import shiroya.order.exception.ProductNotFoundException;
import shiroya.order.exception.UserNotFoundException;
import shiroya.order.feignClient.UserClient;
import shiroya.order.producer.OrderProducer;
import shiroya.order.repo.OrderRepository;
import shiroya.order.security.JwtUtil;
import shiroya.orderEvent.OrderEvent;
import shiroya.userEvent.UserDtoFeing;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final  RestTemplate restTemplate;
    private final OrderRepository repository;
    private final OrderProducer producer;
    private final UserClient userClient;
    private final JwtUtil jwtUtil;

    public Order createOrder(OrderEvent request, HttpServletRequest HttpRequest)
    {
        final String userId = HttpRequest.getAttribute("userId").toString();
        final String url = "http://localhost:8082/product/validateAndReduce";
        final String authHeader = HttpRequest.getHeader("Authorization");
        final String token = "Bearer " + authHeader.substring(7);

        ResponseEntity<Boolean> responseProductAvailability;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<OrderEvent> entity = new HttpEntity<>(request, headers);

        Order saved = null;
        try
        {
            UserDtoFeing user = userClient.getUser(userId, token);
            if (Objects.isNull(user)) {
                log.error("User Not Found"+ getClass());
                throw new UserNotFoundException("User Not Found");
            }
            log.error("User Found Try to Find Product Details for Product: "+request.getProductId()+" in " + getClass());

            responseProductAvailability =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            Boolean.class
                    );

            String email = user.getUserEmail();

            if (Boolean.TRUE.equals(responseProductAvailability.getBody()))
            {
                Order order = new Order();
                order.setUserId(request.getUserId());
                order.setProductId(request.getProductId());
                order.setQuantity(request.getQuantity());
                order.setStatus("CREATED");
                order.setOrderDate(LocalDateTime.now());
                order.setEmail(email);

                saved = repository.save(order);
                log.info("Order Created and Save in DB");
                OrderEvent event = OrderEvent.builder()
                        .orderId(saved.getId().toString())
                        .userId(saved.getUserId())
                        .productId(saved.getProductId())
                        .quantity(saved.getQuantity())
                        .email(saved.getEmail())
                        .status("Created")
                        .build();

                producer.sendOrderEvent(event);
                log.info("Event Sent for kafka Consumer to send mail to user");
                return saved;
            }
        }
        catch (org.springframework.web.client.HttpClientErrorException ex)
        {
        if (ex.getStatusCode().value() == 409)
        {
            throw new InsufficientStockException("Insufficient stock for product: " + request.getProductId());
        }

        if (ex.getStatusCode().value() == 404)
        {
            throw new ProductNotFoundException("Product not found: " + request.getProductId());
        }

        throw new RuntimeException("Client error: " + ex.getMessage());

    } catch (Exception ex)
        {
            ex.getStackTrace();
            throw ex;
    }

    throw new RuntimeException("Unknown error while creating order");
    }
}
