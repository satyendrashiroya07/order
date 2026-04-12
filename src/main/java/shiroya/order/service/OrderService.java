package shiroya.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import shiroya.order.entity.Order;
import shiroya.order.exception.InsufficientStockException;
import shiroya.order.exception.ProductNotFoundException;
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

    public Order createOrder(OrderEvent request)
    {
        final String url = "http://localhost:8082/product/validateAndReduce";

        ResponseEntity<Boolean> responseProductAvailability;

        Order saved = null;
        try
        {
            responseProductAvailability = restTemplate.postForEntity(
                    url,
                    request,
                    Boolean.class
            );

            if (Boolean.TRUE.equals(responseProductAvailability.getBody()))
            {
                Order order = new Order();
                order.setUserId(request.getUserId());
                order.setProductId(request.getProductId());
                order.setQuantity(request.getQuantity());
                order.setStatus("CREATED");
                order.setOrderDate(LocalDateTime.now());
                order.setEmail(request.getEmail());

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
        throw new RuntimeException("Product service is down");
    }

    throw new RuntimeException("Unknown error while creating order");
    }
}
