package AloTra.repository;

import AloTra.Model.ProductDTO; // Sử dụng ProductDTO
import AloTra.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // --- Trang Home ---
    @Query("SELECT new AloTra.Model.ProductDTO(" +
           "p.id, p.shop.id, p.shop.shopName, p.category.id, p.category.name, p.name, p.description, " +
           "p.basePrice, p.stockQuantity, p.soldCount, p.rating, p.reviewCount, p.status, p.createdAt, p.updatedAt) " +
           "FROM Product p JOIN p.shop JOIN p.category " +
           "WHERE p.status = 'ACTIVE' AND p.soldCount > :threshold ")
    Page<ProductDTO> findTopSellingForHome(@Param("threshold") int threshold, Pageable pageable);

    // --- Trang Menu ---
    @Query("SELECT new AloTra.Model.ProductDTO(" +
           "p.id, p.shop.id, p.shop.shopName, p.category.id, p.category.name, p.name, p.description, " +
           "p.basePrice, p.stockQuantity, p.soldCount, p.rating, p.reviewCount, p.status, p.createdAt, p.updatedAt) " +
           "FROM Product p JOIN p.shop JOIN p.category " +
           "WHERE p.status = 'ACTIVE' AND p.soldCount > 0 " +
           "ORDER BY p.soldCount DESC, p.id ASC")
    Page<ProductDTO> findTop10BestSellingForMenu(Pageable pageable);

    @Query("SELECT new AloTra.Model.ProductDTO(" +
           "p.id, p.shop.id, p.shop.shopName, p.category.id, p.category.name, p.name, p.description, " +
           "p.basePrice, p.stockQuantity, p.soldCount, p.rating, p.reviewCount, p.status, p.createdAt, p.updatedAt) " +
           "FROM Product p JOIN p.shop JOIN p.category " +
           "WHERE p.status = 'ACTIVE' AND (:categoryId IS NULL OR p.category.id = :categoryId)")
    Page<ProductDTO> findAllActiveProductsForMenu(@Param("categoryId") Long categoryId, Pageable pageable);

    // --- Trang Quản lý Vendor ---
    /**
     * Tìm sản phẩm theo Shop ID (phân trang).
     */
    @Query("SELECT new AloTra.Model.ProductDTO(" +
           "p.id, p.shop.id, p.shop.shopName, p.category.id, p.category.name, p.name, p.description, " +
           "p.basePrice, p.stockQuantity, p.soldCount, p.rating, p.reviewCount, p.status, p.createdAt, p.updatedAt, " +
           // Subquery để lấy ảnh thumbnail
           "(SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = p AND pi.isThumbnail = true ORDER BY pi.id ASC LIMIT 1) )" +
           "FROM Product p JOIN p.shop JOIN p.category " +
           "WHERE p.shop.id = :shopId")
    Page<ProductDTO> findProductsByShopId(@Param("shopId") Long shopId, Pageable pageable);


    /**
     * Tìm một sản phẩm cụ thể theo ID VÀ ID của shop sở hữu.
     */
    Optional<Product> findByIdAndShop_Id(Long productId, Long shopId);

    /**
     * Kiểm tra sự tồn tại của sản phẩm theo ID VÀ ID của shop sở hữu.
     */
    boolean existsByIdAndShop_Id(Long productId, Long shopId);


    // --- **QUẢN LÝ ADMIN** ---
    /**
     * Lấy tất cả sản phẩm cho Admin (phân trang).
     * Bao gồm thông tin Shop và Category.
     */
     @Query("SELECT new AloTra.Model.ProductDTO(" +
            "p.id, p.shop.id, p.shop.shopName, p.category.id, p.category.name, p.name, p.description, " +
            "p.basePrice, p.stockQuantity, p.soldCount, p.rating, p.reviewCount, p.status, p.createdAt, p.updatedAt, " +
            // Subquery lấy ảnh thumbnail
            "(SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = p AND pi.isThumbnail = true ORDER BY pi.id ASC LIMIT 1) )" +
            "FROM Product p JOIN p.shop s JOIN p.category c") // Không cần điều kiện WHERE cho Admin
     Page<ProductDTO> findAllProductsAdmin(Pageable pageable); // *** THÊM PHƯƠNG THỨC NÀY ***


     /**
      * Kiểm tra xem có sản phẩm nào thuộc về một Category ID cụ thể không.
      * @param categoryId ID của Category.
      * @return true nếu có, false nếu không.
      */
     boolean existsByCategory_Id(Long categoryId); // *** THÊM PHƯƠNG THỨC NÀY ***

}

