package AloTra.repository;

import AloTra.entity.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Thêm import

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    // Tìm sản phẩm đã xem của user (phân trang, sắp xếp theo ngày xem mới nhất)
     @Query("SELECT pv FROM ProductView pv JOIN FETCH pv.product p JOIN FETCH p.shop s " +
           "WHERE pv.account.id = :userId ORDER BY pv.viewedAt DESC")
    Page<ProductView> findUserViewedWithDetails(Long userId, Pageable pageable);

    // Tìm một lượt xem cụ thể (để cập nhật timestamp)
    Optional<ProductView> findByAccount_IdAndProduct_Id(Long userId, Long productId);
}
