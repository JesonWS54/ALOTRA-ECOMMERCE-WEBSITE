package com.alotra.controller.api;

import com.alotra.entity.Review;
import com.alotra.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ReviewApiController - REST API for Product Reviews
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewApiController {

    @Autowired
    private ReviewService reviewService;

    // ==================== CRUD OPERATIONS ====================

    /**
     * GET /api/reviews
     * Lấy tất cả reviews với phân trang
     */
    @GetMapping
    public ResponseEntity<Page<Review>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviews = reviewService.getAllReviews(pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/{id}
     * Lấy review theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        Optional<Review> review = reviewService.getReviewById(id);
        
        if (review.isPresent()) {
            return ResponseEntity.ok(review.get());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("Không tìm thấy review với ID: " + id));
    }

    /**
     * POST /api/reviews
     * Tạo review mới
     */
    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.createReview(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * PUT /api/reviews/{id}
     * Cập nhật review
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @RequestBody Review reviewDetails) {
        try {
            Review updatedReview = reviewService.updateReview(id, reviewDetails);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * DELETE /api/reviews/{id}
     * Xóa review
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa review thành công");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    // ==================== PRODUCT REVIEWS ====================

    /**
     * GET /api/reviews/product/{productId}
     * Lấy reviews của sản phẩm với phân trang
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Review>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Review> reviews = reviewService.getReviewsByProductId(productId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/product/{productId}/rating/{rating}
     * Lấy reviews theo product và rating
     */
    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<Page<Review>> getReviewsByProductAndRating(
            @PathVariable Long productId,
            @PathVariable Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.getReviewsByProductAndRating(productId, rating, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/product/{productId}/verified
     * Lấy verified reviews của product
     */
    @GetMapping("/product/{productId}/verified")
    public ResponseEntity<Page<Review>> getVerifiedReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.getVerifiedReviewsByProduct(productId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/product/{productId}/top
     * Lấy top reviews của product
     */
    @GetMapping("/product/{productId}/top")
    public ResponseEntity<List<Review>> getTopReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewService.getTopReviews(productId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    // ==================== USER REVIEWS ====================

    /**
     * GET /api/reviews/user/{userId}
     * Lấy reviews của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Review>> getReviewsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.getReviewsByUser(userId, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    // ==================== RATING FILTER ====================

    /**
     * GET /api/reviews/rating/{rating}
     * Lấy reviews theo rating
     */
    @GetMapping("/rating/{rating}")
    public ResponseEntity<Page<Review>> getReviewsByRating(
            @PathVariable Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.getReviewsByRating(rating, pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/verified
     * Lấy tất cả verified reviews
     */
    @GetMapping("/verified")
    public ResponseEntity<Page<Review>> getVerifiedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewService.getVerifiedReviews(pageable);
        
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/recent
     * Lấy reviews mới nhất
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Review>> getRecentReviews(
            @RequestParam(defaultValue = "10") int limit) {
        
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewService.getRecentReviews(pageable);
        
        return ResponseEntity.ok(reviews);
    }

    // ==================== STATISTICS ====================

    /**
     * GET /api/reviews/count
     * Đếm tổng số reviews
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countReviews() {
        long total = reviewService.countAllReviews();
        
        Map<String, Long> response = new HashMap<>();
        response.put("totalReviews", total);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reviews/product/{productId}/count
     * Đếm reviews của product
     */
    @GetMapping("/product/{productId}/count")
    public ResponseEntity<Map<String, Long>> countReviewsByProduct(@PathVariable Long productId) {
        long count = reviewService.countReviewsByProduct(productId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("reviewCount", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reviews/user/{userId}/count
     * Đếm reviews của user
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> countReviewsByUser(@PathVariable Long userId) {
        long count = reviewService.countReviewsByUser(userId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("reviewCount", count);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reviews/product/{productId}/average-rating
     * Tính rating trung bình
     */
    @GetMapping("/product/{productId}/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating(@PathVariable Long productId) {
        BigDecimal avgRating = reviewService.getAverageRating(productId);
        long reviewCount = reviewService.countReviewsByProduct(productId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", avgRating);
        response.put("reviewCount", reviewCount);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reviews/product/{productId}/rating-distribution
     * Lấy phân bố rating (5⭐, 4⭐, 3⭐, 2⭐, 1⭐)
     */
    @GetMapping("/product/{productId}/rating-distribution")
    public ResponseEntity<ReviewService.RatingDistribution> getRatingDistribution(
            @PathVariable Long productId) {
        
        ReviewService.RatingDistribution distribution = reviewService.getRatingDistribution(productId);
        return ResponseEntity.ok(distribution);
    }

    // ==================== VALIDATION ====================

    /**
     * GET /api/reviews/user/{userId}/product/{productId}/has-reviewed
     * Kiểm tra user đã review product chưa
     */
    @GetMapping("/user/{userId}/product/{productId}/has-reviewed")
    public ResponseEntity<Map<String, Boolean>> hasUserReviewedProduct(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        
        boolean hasReviewed = reviewService.hasUserReviewedProduct(userId, productId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasReviewed", hasReviewed);
        
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/reviews/{id}/exists
     * Kiểm tra review tồn tại
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Map<String, Boolean>> reviewExists(@PathVariable Long id) {
        boolean exists = reviewService.existsById(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }

    // ==================== TEST ENDPOINT ====================

    /**
     * GET /api/reviews/test
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Review API is running!");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER METHODS ====================

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}