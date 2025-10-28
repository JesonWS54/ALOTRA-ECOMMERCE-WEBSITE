package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List; // Import List

@Data
@NoArgsConstructor
// @AllArgsConstructor // Tạm thời comment hoặc bỏ đi để tránh xung đột nếu dùng constructor tường minh
public class ProductDTO {
    private Long id;
    private Long shopId;
    private String shopName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private Double basePrice;
    private Integer stockQuantity;
    private Integer soldCount;
    private Double rating;
    private Integer reviewCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> images; // Trường này vẫn giữ cho trang chi tiết
    private String thumbnailUrl; // Trường mới cho thumbnail (từ query)

    // Constructor 15 tham số (Dùng cho các query khác không có thumbnail)
    public ProductDTO(Long id, Long shopId, String shopName, Long categoryId, String categoryName, String name,
                      String description, Double basePrice, Integer stockQuantity, Integer soldCount,
                      Double rating, Integer reviewCount, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.shopId = shopId;
        this.shopName = shopName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.stockQuantity = stockQuantity;
        this.soldCount = soldCount;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        // this.images = null; // Hoặc new ArrayList<>();
        // this.thumbnailUrl = null;
    }

    // *** THÊM Constructor 16 tham số (Dùng cho query findProductsByShopId) ***
    public ProductDTO(Long id, Long shopId, String shopName, Long categoryId, String categoryName, String name,
                      String description, Double basePrice, Integer stockQuantity, Integer soldCount,
                      Double rating, Integer reviewCount, String status, LocalDateTime createdAt, LocalDateTime updatedAt,
                      String thumbnailUrl) { // Thêm thumbnailUrl
        this.id = id;
        this.shopId = shopId;
        this.shopName = shopName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.stockQuantity = stockQuantity;
        this.soldCount = soldCount;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.thumbnailUrl = thumbnailUrl; // Gán giá trị thumbnail
        // this.images = null; // Hoặc new ArrayList<>();
    }

    // Lombok @Data sẽ tự tạo getters/setters cho tất cả các trường
}