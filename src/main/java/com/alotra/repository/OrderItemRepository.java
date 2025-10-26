package com.alotra.repository;

import com.alotra.entity.Order;
import com.alotra.entity.OrderItem;
import com.alotra.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderItemRepository
 * Repository for OrderItem entity
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Find all items in an order
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Find items by order ID
     */
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find items by product
     */
    List<OrderItem> findByProduct(Product product);

    /**
     * Count items in order
     */
    long countByOrder(Order order);

    /**
     * Get total quantity ordered for a product
     */
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Integer getTotalQuantityOrderedForProduct(@Param("productId") Long productId);

    /**
     * Get best selling products
     */
    @Query("SELECT oi.product, SUM(oi.quantity) as totalQuantity FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' " +
           "GROUP BY oi.product ORDER BY totalQuantity DESC")
    List<Object[]> findBestSellingProducts();

    /**
     * Get revenue by product
     */
    @Query("SELECT oi.product, SUM(oi.subtotal) as totalRevenue FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' " +
           "AND oi.order.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY oi.product ORDER BY totalRevenue DESC")
    List<Object[]> findRevenueByProduct(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Count orders containing a specific product
     */
    @Query("SELECT COUNT(DISTINCT oi.order) FROM OrderItem oi WHERE oi.product.id = :productId")
    long countOrdersContainingProduct(@Param("productId") Long productId);
}