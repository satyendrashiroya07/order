package shiroya.order.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import shiroya.order.entity.Order;
import shiroya.order.entity.ProcessEntity;
import shiroya.order.repo.OrderRepository;
import shiroya.order.repo.ProcessEntityRepo;
import shiroya.orderEvent.OrderEvent;
import shiroya.orderEvent.OrderStatus;
import shiroya.paymentEvent.PaymentEvent;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class PaymentEventListener {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProcessEntityRepo processEntityRepo;
    @Autowired
    private  KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @KafkaListener(topics = "payment-success1", groupId = "order-group")
    public void handleSuccess(PaymentEvent event) {

        String orderId = event.getOrderId();
        UUID uuid = UUID.fromString(orderId);

        if (processEntityRepo.existsByOrderId(orderId)) {
            log.info("Event already processed: {}", orderId);
            return;
        }

        Order order1 = orderRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order1.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order1);

        OrderEvent event1 = OrderEvent.builder()
                .orderId(saved.getId().toString())
                .userId(saved.getUserId())
                .productId(saved.getProductId())
                .quantity(saved.getQuantity())
                .paymentType("UPI")
                .email(saved.getEmail())
                .status(OrderStatus.CONFIRMED)
                .build();

        kafkaTemplate.send("order-confirmed", event1);

        ProcessEntity processed = new ProcessEntity();
        processed.setOrderId(event.getOrderId());
        processed.setOrderDate(LocalDateTime.now());
        processEntityRepo.save(processed);

        System.out.println("Order CONFIRMED for orderId: " + orderId);
    }

    @KafkaListener(topics = "payment-failed1", groupId = "order-group")
    public void handleFailure(PaymentEvent event) {

        String orderId = event.getOrderId();
        UUID uuid = UUID.fromString(orderId);

        if (processEntityRepo.existsById(orderId)) {
            log.info("Event already processed: {}", orderId);
            return;
        }

        Order order2 = orderRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order2.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order2);

        OrderEvent event1 = OrderEvent.builder()
                .orderId(saved.getId().toString())
                .userId(saved.getUserId())
                .productId(saved.getProductId())
                .quantity(saved.getQuantity())
                .paymentType("UPI")
                .email(saved.getEmail())
                .status(OrderStatus.CANCELLED)
                .build();

        kafkaTemplate.send("order-cancelled", event1);

        ProcessEntity processed = new ProcessEntity();
        processed.setOrderId(event.getOrderId());
        processEntityRepo.save(processed);
        System.out.println("Order CANCELLED for orderId: " + orderId);
    }

    @KafkaListener(topics = "payment-success1.DLQ", groupId = "order-group")
    public void handleSuccessDLQ(PaymentEvent event) {

        log.error("DLQ Event received: {}", event);

        System.out.println("Order in DLQ for orderId: " + event.getOrderId());
    }

    @KafkaListener(topics = "payment-failed1.DLQ", groupId = "order-group")
    public void handleFailureDLQ(PaymentEvent event) {

        log.error("DLQ Event received: {}", event);

        System.out.println("Order in DLQ for orderId: " + event.getOrderId());
    }
}
