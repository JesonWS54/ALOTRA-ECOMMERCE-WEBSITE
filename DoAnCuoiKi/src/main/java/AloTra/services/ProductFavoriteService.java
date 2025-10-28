package AloTra.services;

import AloTra.Model.ProductHomeDTO; // Sử dụng lại DTO này cho gọn
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductFavoriteService {

    /**
     * Lấy danh sách sản phẩm yêu thích của user (phân trang).
     * @param userId ID của user.
     * @param pageable Thông tin phân trang.
     * @return Trang sản phẩm yêu thích (Page<ProductHomeDTO>).
     */
    Page<ProductHomeDTO> findUserFavorites(Long userId, Pageable pageable);

    /**
     * Thêm sản phẩm vào danh sách yêu thích.
     * @param userId ID của user.
     * @param productId ID của sản phẩm.
     * @throws RuntimeException Nếu sản phẩm đã được thích hoặc không tìm thấy.
     */
    void addFavorite(Long userId, Long productId);

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích.
     * @param userId ID của user.
     * @param productId ID của sản phẩm.
     * @throws RuntimeException Nếu sản phẩm chưa được thích hoặc không tìm thấy.
     */
    void removeFavorite(Long userId, Long productId);

    /**
     * Kiểm tra xem sản phẩm có trong danh sách yêu thích của user không.
     * @param userId ID của user.
     * @param productId ID của sản phẩm.
     * @return true nếu đã thích, false nếu chưa.
     */
     boolean isFavorite(Long userId, Long productId);

}
