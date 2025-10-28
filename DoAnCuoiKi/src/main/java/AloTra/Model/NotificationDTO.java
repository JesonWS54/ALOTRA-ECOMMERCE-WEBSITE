package AloTra.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String type; // Loại thông báo (e.g., "NEW_ORDER")
    private String message; // Nội dung thông báo
    private Long orderId; // ID đơn hàng liên quan (tùy chọn)
    private Long shopId; // ID shop nhận (tùy chọn)
    // Thêm các trường khác nếu cần
}