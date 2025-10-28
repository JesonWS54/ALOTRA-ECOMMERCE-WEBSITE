package AloTra.repository;

import AloTra.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Import Optional

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // --- Giữ nguyên các phương thức cũ ---
    Page<Order> findByAccount_Id(Long userId, Pageable pageable);
    Page<Order> findByAccount_IdAndStatus(Long userId, String status, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(o.id) > 0 THEN true ELSE false END " +
           "FROM Order o JOIN o.orderItems oi JOIN oi.product p " +
           "WHERE o.account.id = :userId " +
           "AND p.id = :productId " +
           "AND o.status = :completedStatus")
    boolean checkUserHasPurchasedProduct(@Param("userId") Long userId,
                                         @Param("productId") Long productId,
                                         @Param("completedStatus") String completedStatus);

    // --- Phương thức mới cho Vendor ---
    Page<Order> findByShop_Id(Long shopId, Pageable pageable); // Đã sửa tên
    Page<Order> findByShop_IdAndStatus(Long shopId, String status, Pageable pageable); // Đã sửa tên

    // --- **THÊM PHƯƠNG THỨC TÍNH DOANH THU** ---
    @Query("SELECT SUM(o.finalTotal) FROM Order o WHERE o.shop.id = :shopId AND o.status = 'COMPLETED'")
    Optional<Double> calculateTotalCompletedRevenueByShop(@Param("shopId") Long shopId);
    // ------------------------------------------

}

