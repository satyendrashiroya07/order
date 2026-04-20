package shiroya.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shiroya.order.kafkaConfig.OutboxEvent;

import java.util.List;

@Repository
public interface OutBoxEventRepo extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findByStatus(String aNew);
}
