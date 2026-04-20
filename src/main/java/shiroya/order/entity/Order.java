package shiroya.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import shiroya.orderEvent.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 100, nullable = false)
    private String userId;

    @Column(length = 100, nullable = false)
    private String productId;

    @Column(length = 20, nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private LocalDateTime orderDate;

    @Column(length = 100, nullable = false)
    private String email;
}
