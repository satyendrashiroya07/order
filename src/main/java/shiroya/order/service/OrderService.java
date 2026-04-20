package shiroya.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import shiroya.order.entity.Order;
import shiroya.order.exception.InsufficientStockException;
import shiroya.order.exception.ProductNotFoundException;
import shiroya.order.exception.UserNotFoundException;
import shiroya.order.feignClient.UserClient;
import shiroya.order.kafkaConfig.OutboxEvent;
import shiroya.order.producer.OrderProducer;
import shiroya.order.repo.OrderRepository;
import shiroya.order.repo.OutBoxEventRepo;
import shiroya.orderEvent.OrderEvent;
import shiroya.orderEvent.OrderRequest;
import shiroya.orderEvent.OrderStatus;
import shiroya.productEvent.ProductResponse;
import shiroya.userEvent.UserDtoFeing;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final  RestTemplate restTemplate;
    private final OrderRepository repository;
    private final OrderProducer producer;
    private final UserClient userClient;
    private final OutBoxEventRepo outBoxEventRepo;
    private final ObjectMapper objectMapper;
    private final ProductClientService productClientService;


    @Transactional
    public Order createOrder(OrderRequest request, HttpServletRequest HttpRequest)
    {
        final String userId = HttpRequest.getAttribute("userId").toString();
        final String authHeader = HttpRequest.getHeader("Authorization");
        final String token = "Bearer " + authHeader.substring(7);

        ResponseEntity<ProductResponse> responseProductAvailability;

        Order saved = null;
        try
        {
            UserDtoFeing user = userClient.getUser(userId, token);
            if (Objects.isNull(user)) {
                log.error("User Not Found"+ getClass());
                throw new UserNotFoundException("User Not Found");
            }
            log.error("User Found Try to Find Product Details for Product: "+request.getProductId()+" in " + getClass());

            ProductResponse product =
                    productClientService.checkProduct(request, token);

            String email = user.getUserEmail();
            double amount = product.getPrice()
                    .multiply(BigDecimal.valueOf(request.getQuantity())).doubleValue();;

            if (Objects.nonNull(product) && product.getQuantity() >= request.getQuantity())
            {
                Order order = new Order();
                order.setUserId(request.getUserId());
                order.setProductId(request.getProductId());
                order.setQuantity(request.getQuantity());
                order.setStatus(OrderStatus.PAYMENT_PENDING);
                order.setOrderDate(LocalDateTime.now());
                order.setEmail(email);

                saved = repository.save(order);
                log.info("Order Created and Saved in DB");

                OrderEvent event = OrderEvent.builder()
                        .orderId(saved.getId().toString())
                        .userId(saved.getUserId())
                        .productId(saved.getProductId())
                        .quantity(saved.getQuantity())
                        .amount(amount)
                        .paymentType("UPI")
                        .email(saved.getEmail())
                        .status(OrderStatus.PAYMENT_PENDING)
                        .build();

                OutboxEvent kafkaDbEvent = new OutboxEvent();
                kafkaDbEvent.setAggregateType("ORDER");
                kafkaDbEvent.setAggregateId(order.getId().toString());
                kafkaDbEvent.setEventType("ORDER_CREATED");
                kafkaDbEvent.setPayload(convertToJson(event));
                kafkaDbEvent.setStatus("NEW");

                outBoxEventRepo.save(kafkaDbEvent);

                return saved;
            }
            else
            {
                throw new InsufficientStockException("Insufficient stock for product: " + request.getProductId());
            }
        }
        catch (org.springframework.web.client.HttpClientErrorException ex)
        {
        throw new RuntimeException("Client error: " + ex.getMessage());

    } catch (Exception ex)
        {
            ex.getStackTrace();
            throw ex;
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {

        List<OutboxEvent> events = outBoxEventRepo.findByStatus("NEW");

        for (OutboxEvent event : events) {
            try {

                OrderEvent orderEvent =
                        objectMapper.readValue(event.getPayload(), OrderEvent.class);

                producer.sendOrderEvent(orderEvent);

                event.setStatus("SENT");
                outBoxEventRepo.save(event);

            } catch (Exception e) {
                log.error("Failed to publish event {}", event.getId(), e);
            }
        }
    }


    private String convertToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}
