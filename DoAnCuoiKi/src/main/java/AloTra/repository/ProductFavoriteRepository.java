package AloTra.repository;

import AloTra.entity.ProductFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional; // Cần import Optional

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, Long> {

    /**
     * Tìm danh sách sản phẩm yêu thích của user, kèm thông tin Product và Shop.
     * Sắp xếp theo ngày thêm mới nhất (dùng addedAt).
     * @param userId ID của người dùng
     * @param pageable Thông tin phân trang
     * @return Trang danh sách ProductFavorite
     */
    @Query("SELECT pf FROM ProductFavorite pf " +
           "JOIN FETCH pf.product p " +
           "JOIN FETCH p.shop s " +
           "WHERE pf.account.id = :userId " +
           "ORDER BY pf.addedAt DESC")
    Page<ProductFavorite> findUserFavoritesWithDetails(@Param("userId") Long userId, Pageable pageable);

    /**
     * Tìm một bản ghi ProductFavorite dựa trên userId và productId.
     * Dùng Optional để xử lý trường hợp không tìm thấy.
     * @param userId ID của người dùng
     * @param productId ID của sản phẩm
     * @return Optional chứa ProductFavorite nếu tìm thấy, ngược lại là Optional rỗng.
     */
    Optional<ProductFavorite> findByAccount_IdAndProduct_Id(Long userId, Long productId);

    // boolean existsByAccount_IdAndProduct_Id(Long userId, Long productId); // Cách khác để kiểm tra tồn tại
    // void deleteByAccount_IdAndProduct_Id(Long userId, Long productId); // Cách khác để xóa (cần @Modifying @Transactional)
}

