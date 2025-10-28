package AloTra.Model; 

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor // Lombok sẽ tự tạo constructor full tham số
public class ProductHomeDTO {
    private Long id;
    private String name;
    private Double basePrice;
    private String shopName;
    private String imageUrl; // Sẽ là ảnh thumbnail
    private Integer soldCount;
    private Long favoriteCount; // Thêm trường này

    // Constructor cũ (6 tham số) - Dùng cho các query không lấy favorite count
    public ProductHomeDTO(Long id, String name, Double basePrice, String shopName, String imageUrl, Integer soldCount) {
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.shopName = shopName;
        this.imageUrl = imageUrl;
        this.soldCount = soldCount;
        this.favoriteCount = null; // Mặc định là null nếu không được query
    }

    // Constructor mới (7 tham số) sẽ được Lombok @AllArgsConstructor tạo ra
    // Hoặc bạn có thể tự viết tường minh nếu muốn:
    // public ProductHomeDTO(Long id, String name, Double basePrice, String shopName, String imageUrl, Integer soldCount, Long favoriteCount) { ... }

}

