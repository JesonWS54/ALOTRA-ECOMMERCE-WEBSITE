package AloTra.services;

import AloTra.Model.ReviewDTO; // Sử dụng DTO bạn đã cung cấp
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ReviewService {

    /**
     * Lấy danh sách đánh giá cho một sản phẩm (phân trang).
     * @param productId ID sản phẩm
     * @param pageable Thông tin phân trang
     * @return Trang các đánh giá DTO
     */
    Page<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable);

    // (Sau này cần thêm) Hàm thêm đánh giá mới
    // ReviewDTO addReview(Long userId, Long productId, Long orderItemId, int rating, String comment, List<MultipartFile> mediaFiles);

    // (Sau này cần thêm) Hàm kiểm tra user đã mua sản phẩm chưa
    // boolean hasUserPurchasedProduct(Long userId, Long productId);
    ReviewDTO addReview(Long userId, Long productId, Integer rating, String comment, List<MultipartFile> mediaFiles) throws Exception;
}
