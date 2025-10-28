package AloTra.repository;

import AloTra.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List; // Import List
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Tìm OrderItem từ đơn hàng hoàn thành, theo user và product.
     * Sửa: Bỏ điều kiện NOT EXISTS để test
     * @param userId ID của User.
     * @param productId ID của Product.
     * @param completedStatus Trạng thái đơn hàng hoàn thành (vd: "COMPLETED").
     * @return Optional<OrderItem> nếu tìm thấy.
     */
    @Query("SELECT oi FROM OrderItem oi JOIN oi.order o JOIN oi.product p " +
           "WHERE o.account.id = :userId " +
           "AND p.id = :productId " +
           "AND o.status = :completedStatus " +
           //"AND NOT EXISTS (SELECT 1 FROM Review r WHERE r.orderItem = oi)" + // Tạm thời bỏ qua
           "ORDER BY o.createdAt DESC") // Lấy order item từ đơn hàng gần nhất
    Optional<OrderItem> findCompletedOrderItem(@Param("userId") Long userId,
                                               @Param("productId") Long productId,
                                               @Param("completedStatus") String completedStatus);

    /**
     * Lấy danh sách OrderItem theo Order ID.
     * @param orderId ID của Order.
     * @return Danh sách OrderItem.
     */
    List<OrderItem> findByOrder_Id(Long orderId); // <-- THÊM PHƯƠNG THỨC NÀY

}
