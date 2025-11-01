package nhom12.AloTra.repository;

import nhom12.AloTra.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
    List<OrderStatusHistory> findByDonHang_MaDonHangOrderByThoiDiemThayDoiDesc(Long orderId);
}
