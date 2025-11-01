package nhom12.AloTra.service;

import nhom17.OneShop.entity.Product;
import nhom17.OneShop.request.ProductRequest;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.List; // QUAN TRỌNG: Thêm import này

public interface ProductService {
    // Phương thức cho trang Admin
    Page<Product> searchProducts(String keyword, Boolean status, Integer categoryId, Integer brandId, String sort, int page, int size);
    Page<Product> searchUserProducts(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sort, List<Integer> brandIds, int page, int size);

    // PHƯƠNG THỨC MỚI CHO TÌM KIẾM HEADER
    Page<Product> searchProductsForUser(String keyword, int page, int size);
    Product findById(int id);

    void save(ProductRequest productRequest);
    void delete(int id);

    List<Product> findNewestProducts(int limit);
    List<Product> findMostDiscountedProducts(int limit);
    List<Product> findTopSellingProducts(int limit);
}