package com.alotra.service;

import com.alotra.entity.Product;
import com.alotra.entity.Review;
import com.alotra.entity.User;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.ReviewRepository;
import com.alotra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ReviewService - FIXED VERSION
 */
@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Tạo review mới
     */
    public Review createReview(Review review) {
        // Validate
        validateReview(review);

        // Check if user already reviewed this product
        if (hasUserReviewedProduct(review.getUser().getId(), review.getProduct().getId())) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Set timestamps
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        System.out.println("✅ Review mới: " + review.getProduct().getName() + " - " + review.getRating() + "⭐");

        return savedReview;
    }

    /**
     * Lấy review theo ID
     */
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    /**
     * Lấy tất cả reviews
     */
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    /**
     * Lấy reviews với phân trang
     */
    public Page<Review> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable);
    }

    /**
     * Cập nhật review
     */
    public Review updateReview(Long id, Review reviewDetails) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy review với ID: " + id));

        // Chỉ cho phép update rating và comment
        if (reviewDetails.getRating() != null) {
            validateRating(reviewDetails.getRating());
            review.setRating(reviewDetails.getRating());
        }

        if (reviewDetails.getComment() != null) {
            review.setComment(reviewDetails.getComment());
        }

        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);

        // Update product rating
        updateProductRating(review.getProduct().getId());

        return updatedReview;
    }

    /**
     * Xóa review
     */
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy review với ID: " + id));

        Long productId = review.getProduct().getId();

        reviewRepository.delete(review);

        // Update product rating after deletion
        updateProductRating(productId);

        System.out.println("✅ Đã xóa review");
    }

    // ==================== QUERY OPERATIONS ====================

    /**
     * Lấy reviews theo product
     */
    public List<Review> getReviewsByProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        return reviewRepository.findByProduct(product);
    }

    /**
     * Lấy reviews theo product với phân trang
     */
    public Page<Review> getReviewsByProduct(Long productId, Pageable pageable) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        return reviewRepository.findByProduct(product, pageable);
    }

    /**
     * Lấy reviews theo product ID
     */
    public Page<Review> getReviewsByProductId(Long productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }

    /**
     * Lấy reviews theo user
     */
    public List<Review> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        return reviewRepository.findByUser(user);
    }

    /**
     * Lấy reviews theo user với phân trang
     */
    public Page<Review> getReviewsByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        return reviewRepository.findByUser(user, pageable);
    }

    /**
     * Lấy reviews theo rating
     */
    public Page<Review> getReviewsByRating(Integer rating, Pageable pageable) {
        validateRating(rating);
        return reviewRepository.findByRating(rating, pageable);
    }

    /**
     * Lấy reviews theo product và rating
     */
    public Page<Review> getReviewsByProductAndRating(Long productId, Integer rating, Pageable pageable) {
        validateRating(rating);
        return reviewRepository.findByProductIdAndRating(productId, rating, pageable);
    }

    /**
     * Lấy verified reviews
     */
    public Page<Review> getVerifiedReviews(Pageable pageable) {
        return reviewRepository.findByIsVerifiedPurchase(true, pageable);
    }

    /**
     * Lấy verified reviews của product
     */
    public Page<Review> getVerifiedReviewsByProduct(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndIsVerifiedPurchase(productId, true, pageable);
    }

    /**
     * Lấy top reviews của product
     */
    public List<Review> getTopReviews(Long productId, Pageable pageable) {
        return reviewRepository.findTopReviewsByProduct(productId, pageable);
    }

    /**
     * Lấy recent reviews
     */
    public List<Review> getRecentReviews(Pageable pageable) {
        return reviewRepository.findRecentReviews(pageable);
    }

    // ==================== STATISTICS ====================

    /**
     * Đếm tổng số reviews
     */
    public long countAllReviews() {
        return reviewRepository.count();
    }

    /**
     * Đếm reviews của product
     */
    public long countReviewsByProduct(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    /**
     * Đếm reviews của user
     */
    public long countReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

        return reviewRepository.countByUser(user);
    }

    /**
     * Tính rating trung bình của product
     */
    public BigDecimal getAverageRating(Long productId) {
        BigDecimal avgRating = reviewRepository.getAverageRatingByProduct(productId);
        
        if (avgRating == null) {
            return BigDecimal.ZERO;
        }

        // Round to 1 decimal place
        return avgRating.setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * Lấy rating distribution của product
     */
    public RatingDistribution getRatingDistribution(Long productId) {
        long total = reviewRepository.countByProductId(productId);
        long star5 = reviewRepository.countByProductIdAndRating(productId, 5);
        long star4 = reviewRepository.countByProductIdAndRating(productId, 4);
        long star3 = reviewRepository.countByProductIdAndRating(productId, 3);
        long star2 = reviewRepository.countByProductIdAndRating(productId, 2);
        long star1 = reviewRepository.countByProductIdAndRating(productId, 1);

        return new RatingDistribution(total, star5, star4, star3, star2, star1);
    }

    // ==================== BUSINESS LOGIC ====================

    /**
     * Kiểm tra user đã review product chưa
     */
    public boolean hasUserReviewedProduct(Long userId, Long productId) {
        return reviewRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * Update product rating sau khi có review mới/update/delete
     */
    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // Calculate new average rating
        BigDecimal avgRating = getAverageRating(productId);
        long reviewCount = countReviewsByProduct(productId);

        // Update product
        product.setRating(avgRating);
        product.setReviewCount((int) reviewCount);
        product.setUpdatedAt(LocalDateTime.now());

        productRepository.save(product);

        System.out.println("✅ Updated product rating: " + product.getName() + " - " + avgRating + "⭐ (" + reviewCount + " reviews)");
    }

    // ==================== VALIDATION ====================

    /**
     * Validate review
     */
    private void validateReview(Review review) {
        if (review.getUser() == null || review.getUser().getId() == null) {
            throw new RuntimeException("User is required");
        }

        if (review.getProduct() == null || review.getProduct().getId() == null) {
            throw new RuntimeException("Product is required");
        }

        if (review.getRating() == null) {
            throw new RuntimeException("Rating is required");
        }

        validateRating(review.getRating());

        // Validate user exists
        if (!userRepository.existsById(review.getUser().getId())) {
            throw new RuntimeException("User không tồn tại");
        }

        // Validate product exists
        if (!productRepository.existsById(review.getProduct().getId())) {
            throw new RuntimeException("Product không tồn tại");
        }
    }

    /**
     * Validate rating (1-5)
     */
    private void validateRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating phải từ 1 đến 5");
        }
    }

    // ==================== UTILITIES ====================

    /**
     * Check if review exists
     */
    public boolean existsById(Long id) {
        return reviewRepository.existsById(id);
    }

    // ==================== INNER CLASS ====================

    /**
     * Rating Distribution DTO
     */
    public static class RatingDistribution {
        private long totalReviews;
        private long star5;
        private long star4;
        private long star3;
        private long star2;
        private long star1;

        public RatingDistribution(long totalReviews, long star5, long star4, long star3, long star2, long star1) {
            this.totalReviews = totalReviews;
            this.star5 = star5;
            this.star4 = star4;
            this.star3 = star3;
            this.star2 = star2;
            this.star1 = star1;
        }

        // Getters
        public long getTotalReviews() { return totalReviews; }
        public long getStar5() { return star5; }
        public long getStar4() { return star4; }
        public long getStar3() { return star3; }
        public long getStar2() { return star2; }
        public long getStar1() { return star1; }

        // Calculate percentages
        public double getStar5Percent() {
            return totalReviews > 0 ? (star5 * 100.0 / totalReviews) : 0;
        }

        public double getStar4Percent() {
            return totalReviews > 0 ? (star4 * 100.0 / totalReviews) : 0;
        }

        public double getStar3Percent() {
            return totalReviews > 0 ? (star3 * 100.0 / totalReviews) : 0;
        }

        public double getStar2Percent() {
            return totalReviews > 0 ? (star2 * 100.0 / totalReviews) : 0;
        }

        public double getStar1Percent() {
            return totalReviews > 0 ? (star1 * 100.0 / totalReviews) : 0;
        }
    }
}