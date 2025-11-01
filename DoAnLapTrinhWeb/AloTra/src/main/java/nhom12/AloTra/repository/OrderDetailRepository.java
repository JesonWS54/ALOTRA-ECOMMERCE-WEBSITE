package nhom12.AloTra.repository;

import nhom12.AloTra.entity.OrderDetail;
import nhom12.AloTra.entity.OrderDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {
}