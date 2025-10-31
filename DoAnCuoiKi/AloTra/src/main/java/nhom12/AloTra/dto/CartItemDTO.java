package nhom12.AloTra.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Integer productId;
    private String productName;
    private String productImage;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public static CartItemDTO fromEntity(nhom12.AloTra.entity.Cart cartEntity) {
        CartItemDTO dto = new CartItemDTO();
        if (cartEntity.getSanPham() != null) {
            dto.setProductId(cartEntity.getSanPham().getMaSanPham());
            dto.setProductName(cartEntity.getSanPham().getTenSanPham());
            dto.setProductImage(cartEntity.getSanPham().getHinhAnh());
        }
        dto.setQuantity(cartEntity.getSoLuong());
        dto.setUnitPrice(cartEntity.getDonGia());
        dto.setLineTotal(cartEntity.getThanhTien());
        return dto;
    }
}