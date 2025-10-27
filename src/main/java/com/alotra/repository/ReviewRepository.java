package com.alotra.repository;

import com.alotra.entity.Product;
import com.alotra.entity.Review;
import com.alotra.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * ReviewRepository - COMPLETE VERSION
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // ==================== BASIC FINDERS ====================

    /**
     * Tìm reviews theo product
     */
    List<Review> findByProduct(Product product);
    
    /**
     * Tìm reviews theo product với phân trang
     */
    Page<Review> findByProduct(Product product, Pageable pageable);
    
    /**
     * Tìm reviews theo product ID với phân trang
     */
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    /**
     * Tìm reviews theo user
     */
    List<Review> findByUser(User user);
    
    /**
     * Tìm reviews theo user với phân trang
     */
    Page<Review> findByUser(User user, Pageable pageable);
    
    /**
     * Tìm reviews theo rating
     */
    List<Review> findByRating(Integer rating);
    
    /**
     * Tìm reviews theo rating với phân trang
     */
    Page<Review> findByRating(Integer rating, Pageable pageable);
    
    /**
     * Tìm reviews theo product và rating
     */
    Page<Review> findByProductIdAndRating(Long productId, Integer rating, Pageable pageable);
    
    /**
     * Tìm verified purchase reviews
     */
    Page<Review> findByIsVerifiedPurchase(Boolean isVerified, Pageable pageable);
    
    /**
     * Tìm verified reviews của product
     */
    Page<Review> findByProductIdAndIsVerifiedPurchase(Long productId, Boolean isVerified, Pageable pageable);

    // ==================== COUNTING ====================
    
    /**
     * Đếm reviews theo product ID
     */
    long countByProductId(Long productId);
    
    /**
     * Đếm reviews theo user
     */
    long countByUser(User user);
    
    /**
     * Đếm reviews theo product ID và rating
     */
    long countByProductIdAndRating(Long productId, Integer rating);
    
    // ==================== EXISTENCE CHECK ====================
    
    /**
     * Kiểm tra user đã review product chưa
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // ==================== CUSTOM QUERIES ====================
    
    /**
     * Tính rating trung bình của product
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    BigDecimal getAverageRatingByProduct(@Param("productId") Long productId);
    
    /**
     * Tìm reviews có images (nếu Review có field images)
     * NOTE: Nếu Review không có field images, comment query này
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.images IS NOT NULL AND r.images != ''")
    Page<Review> findReviewsWithImages(@Param("productId") Long productId, Pageable pageable);
    
    /**
     * Tìm top reviews (rating cao + helpful count cao)
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.rating DESC, r.createdAt DESC")
    List<Review> findTopReviewsByProduct(@Param("productId") Long productId, Pageable pageable);
    
    /**
     * Tìm recent reviews
     */
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findRecentReviews(Pageable pageable);
}