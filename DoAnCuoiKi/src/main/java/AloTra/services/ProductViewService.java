package AloTra.services;

import AloTra.Model.ProductHomeDTO; // Dùng lại DTO này
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductViewService {

    /**
     * Lấy danh sách sản phẩm đã xem của user (phân trang).
     * @param userId ID của user.
     * @param pageable Thông tin phân trang.
     * @return Trang sản phẩm đã xem (Page<ProductHomeDTO>).
     */
    Page<ProductHomeDTO> findUserViewedProducts(Long userId, Pageable pageable);

    /**
     * Ghi nhận lượt xem sản phẩm.
     * Nếu đã xem, cập nhật thời gian xem mới nhất.
     * Nếu chưa xem, tạo bản ghi mới.
     * @param userId ID của user.
     * @param productId ID của sản phẩm.
     */
    void recordProductView(Long userId, Long productId);
}