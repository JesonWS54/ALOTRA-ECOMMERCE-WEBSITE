package AloTra.Model;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartViewDTO {
    private Long cartId; // ID của giỏ hàng
    private Long userId; // ID của user sở hữu
    private List<CartItemDTO> items; // Danh sách các sản phẩm trong giỏ
    private double subtotal; // Tạm tính (tổng tiền hàng)
    private double shippingCost; // Phí vận chuyển
    private double tax; // Thuế (nếu có)
    private double discount; // Số tiền giảm giá từ voucher
    private double total; // Tổng tiền cuối cùng
    private int totalItems; // Tổng số loại sản phẩm trong giỏ

    // *** THÊM TRƯỜNG NÀY ***
    private VoucherDTO appliedVoucher; // Thông tin voucher đã áp dụng (null nếu không có)
}