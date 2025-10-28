package AloTra.services;

import AloTra.Model.ProductDTO; // Sử dụng ProductDTO
import AloTra.Model.ProductHomeDTO; // Import ProductHomeDTO
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import java.io.IOException; // Import IOException
import java.util.List;
import java.util.Optional; // Import Optional

public interface ProductService {

    // --- Trang Home ---
    Page<ProductDTO> getTopSellingProductsForHome(Pageable pageable);

    // --- Trang Menu ---
    Page<ProductDTO> getTop10BestSellingForMenu(Pageable pageable);
    Page<ProductDTO> getAllActiveProductsForMenu(Long categoryId, Pageable pageable);

    // --- Trang Chi tiết Sản phẩm ---
    Optional<ProductDTO> getProductDetails(Long productId); // Dùng Optional để xử lý not found

    // --- Trang Quản lý Vendor ---
    Page<ProductDTO> getProductsByShopId(Long shopId, Pageable pageable);

    @Transactional
    ProductDTO addProduct(Long shopId, ProductDTO productDTO, List<MultipartFile> imageFiles) throws IOException;

    Optional<ProductDTO> getEditProductDetails(Long productId, Long shopId);

    @Transactional
    ProductDTO updateProduct(Long productId, Long shopId, ProductDTO productDTO, List<MultipartFile> imageFiles) throws IOException;

    @Transactional
    void deleteProduct(Long productId, Long shopId);

    // --- **QUẢN LÝ ADMIN** ---
    /**
     * Lấy tất cả sản phẩm từ tất cả các shop (dùng cho Admin).
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Trang các ProductDTO.
     */
    Page<ProductDTO> getAllProductsAdmin(Pageable pageable); // *** THÊM ***

    /**
     * Cập nhật thông tin sản phẩm bởi Admin (không bao gồm ảnh).
     * @param productId ID sản phẩm cần cập nhật.
     * @param productDTO Dữ liệu mới (tên, mô tả, giá, kho, category, status).
     * @return ProductDTO đã cập nhật.
     * @throws IOException (Mặc dù không xử lý file, giữ lại để đồng bộ signature).
     * @throws RuntimeException Nếu không tìm thấy sản phẩm hoặc category.
     */
    @Transactional
    ProductDTO updateProductAdmin(Long productId, ProductDTO productDTO) throws IOException; // *** THÊM ***

    /**
     * Xóa sản phẩm bởi Admin.
     * @param productId ID sản phẩm cần xóa.
     * @throws RuntimeException Nếu không tìm thấy sản phẩm.
     */
    @Transactional
    void deleteProductAdmin(Long productId); // *** THÊM ***


    // --- Các hàm deprecated (để tránh lỗi compile ở nơi khác gọi) ---
    @Deprecated
    List<ProductHomeDTO> getBestSellingProducts();

    @Deprecated
    Page<ProductHomeDTO> getProducts(Long categoryId, Long userId, String sortType, int page, int size);


}

