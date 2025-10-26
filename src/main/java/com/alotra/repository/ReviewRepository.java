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

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);
    
    Page<Review> findByProduct(Product product, Pageable pageable);
    
    List<Review> findByProductId(Long productId);
    
    Page<Review> findByProductId(Long productId, Pageable pageable);
    
    List<Review> findByUser(User user);
    
    Page<Review> findByUser(User user, Pageable pageable);
    
    List<Review> findByUserId(Long userId);
    
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);
    
    boolean existsByProductIdAndUserId(Long productId, Long userId);
    
    List<Review> findByRating(Integer rating);
    
    Page<Review> findByProductIdAndRating(Long productId, Integer rating, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    Page<Review> findRecentReviewsByProductId(@Param("productId") Long productId, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId AND r.isVerifiedPurchase = true")
    Page<Review> findVerifiedReviewsByProductId(@Param("productId") Long productId, Pageable pageable);
    
    @Query("SELECT r FROM Review r ORDER BY r.helpfulCount DESC, r.createdAt DESC")
    Page<Review> findMostHelpfulReviews(Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> getRatingDistribution(@Param("productId") Long productId);
    
    long countByProductId(Long productId);
    
    long countByUserId(Long userId);
}