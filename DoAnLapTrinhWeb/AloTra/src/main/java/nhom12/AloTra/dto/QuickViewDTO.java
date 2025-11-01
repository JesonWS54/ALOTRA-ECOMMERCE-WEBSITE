package nhom12.AloTra.dto;

import java.util.List;

public class QuickViewDTO {
    public Integer id;
    public String name;
    public String brandName;
    public String categoryName;
    public String shortDesc;
    public Long price;
    public Long oldPrice;
    public boolean inStock;
    public Integer soLuongTon;
    public Double rating;
    public Integer reviewCount;
    public List<String> images;

    public QuickViewDTO(Integer id, String name, String brandName, String categoryName,
                        String shortDesc, Long price, Long oldPrice, boolean inStock, Integer soLuongTon,
                        Double rating, Integer reviewCount, List<String> images) {
        this.id = id; this.name = name; this.brandName = brandName; this.categoryName = categoryName;
        this.shortDesc = shortDesc; this.price = price; this.oldPrice = oldPrice; this.inStock = inStock;
        this.soLuongTon = soLuongTon; // <-- THÊM DÒNG NÀY
        this.rating = rating; this.reviewCount = reviewCount; this.images = images;
    }
}