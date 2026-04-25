package shiroya.order.kafkaConfig;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private String status;

    private String traceId;
}
