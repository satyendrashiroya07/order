package shiroya.order.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shiroya.order.entity.Order;
import shiroya.order.repo.OrderRepository;
import shiroya.paymentEvent.PaymentEvent;

import java.util.UUID;

@Component
public class PaymentEventListener {

    @Autowired
    private OrderRepository orderRepository;

    @KafkaListener(topics = "payment-success1", groupId = "order-group")
    public void handleSuccess(PaymentEvent event) {

        String orderId = event.getOrderId();
        UUID uuid = UUID.fromString(orderId);

        Order order1 = orderRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order1.setStatus("CONFIRMED");
        orderRepository.save(order1);

        System.out.println("Order CONFIRMED for orderId: " + orderId);
    }

    @KafkaListener(topics = "payment-failed1", groupId = "order-group")
    public void handleFailure(PaymentEvent event) {

        String orderId = event.getOrderId();
        UUID uuid = UUID.fromString(orderId);

        Order order2 = orderRepository.findById(uuid)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order2.setStatus("CANCELLED");
        orderRepository.save(order2);

        System.out.println("Order CANCELLED for orderId: " + orderId);
    }
}
