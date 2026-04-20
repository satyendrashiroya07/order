package shiroya.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
public class ProcessEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 200, nullable = false, unique = true)
    private String orderId;

    private LocalDateTime orderDate;
}
