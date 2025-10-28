package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherDTO {
    private Long id;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double maxDiscountAmount;
    private Double minOrderValue;
    private Integer quantity;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String createdByRole;
    private Long shopId;
    // *** SỬA LỖI: Đổi kiểu dữ liệu thành String ***
    private String shopName; // Tên shop phải là String
}
