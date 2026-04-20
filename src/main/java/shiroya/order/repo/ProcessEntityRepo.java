package shiroya.order.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shiroya.order.entity.ProcessEntity;

@Repository
public interface ProcessEntityRepo extends JpaRepository<ProcessEntity,String> {
    boolean existsByOrderId(String orderId);
}
