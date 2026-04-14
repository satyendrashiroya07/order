package shiroya.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import productEvent.userEvent.UserDtoFeing;
import shiroya.order.entity.Order;
import shiroya.order.exception.InsufficientStockException;
import shiroya.order.exception.ProductNotFoundException;
import shiroya.order.exception.UserNotFoundException;
import shiroya.order.feignClient.UserClient;
import shiroya.order.producer.OrderProducer;
import shiroya.order.repo.OrderRepository;
import shiroya.orderEvent.OrderEvent;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final  RestTemplate restTemplate;
    private final OrderRepository repository;
    private final OrderProducer producer;
    private final UserClient userClient;

    public Order createOrder(OrderEvent request, String userId)
    {
        final String url = "http://localhost:8082/product/validateAndReduce";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Gateway-Secret", "mysecret");
        //headers.set("Authorization", "Bearer dummy-token");

        HttpEntity<OrderEvent> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Boolean> responseProductAvailability;

        Order saved = null;
        try
        {
             responseProductAvailability =
                    restTemplate.exchange(
                            url,
                            HttpMethod.POST,
                            entity,
                            Boolean.class
                    );

            UserDtoFeing user = userClient.getUser(userId,"mysecret");

            if (Objects.isNull(user)) {
                throw new UserNotFoundException("User not found");
            }

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

                // Publish event
                OrderEvent event = OrderEvent.builder()
                        .orderId(saved.getId().toString())
                        .userId(saved.getUserId())
                        .productId(saved.getProductId())
                        .quantity(saved.getQuantity())
                        .email(saved.getEmail())
                        .status("Created")
                        .build();

                producer.sendOrderEvent(event);

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
