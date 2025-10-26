package com.alotra.repository;

import com.alotra.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByProductId(Long productId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.user.id = :userId")
    List<OrderItem> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT oi.product.id, oi.productName, SUM(oi.quantity) as totalSold, SUM(oi.subtotal) as revenue " +
           "FROM OrderItem oi JOIN oi.order o " +
           "WHERE o.status = 'DELIVERED' AND o.createdAt >= :startDate " +
           "GROUP BY oi.product.id, oi.productName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findBestSellingProducts(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(DISTINCT oi.product.id) FROM OrderItem oi")
    long countDistinctProducts();
}