package AloTra.Model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List; // Import List

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;

    // Thông tin User (Người đặt) - Sửa tên cho rõ ràng hơn
    private Long accountId; // Giữ nguyên từ CSDL
    private String accountUsername; // Lấy từ Account Entity

    // Thông tin Shop (Người bán) - Sửa tên cho rõ ràng hơn
    private Long shopId; // Đổi tên từ shop -> shopId
    private String shopName; // Lấy từ Shop Entity

    // Thông tin Giao hàng (Snapshot)
    private String shippingAddress;
    private String shippingPhone;
    private String shippingFullName;

    // Thông tin Nhà vận chuyển
    private Long shippingCarrierId;
    private String shippingCarrierName; // Cần lấy từ ShippingCarrier Entity (Nếu có)
    private Double shippingFee;

    // Thông tin Giá trị đơn hàng
    private Double itemsTotalPrice; // Tổng tiền hàng (trước giảm giá, phí)
    private Double voucherDiscount; // Số tiền giảm từ voucher
    private Double commissionFee; // Phí hoa hồng (nếu có)
    private Double finalTotal; // Tổng cuối cùng khách trả

    // Trạng thái
    private String status; // Trạng thái đơn hàng (PENDING, CONFIRMED...)
    private String paymentMethod; // Phương thức thanh toán (COD, VNPAY...)
    private String paymentStatus; // Trạng thái thanh toán (UNPAID, PAID...)

    // Thông tin khác
    private String notes; // Ghi chú của khách

    // Thông tin Shipper (Nếu có)   
    private Long shipperId;
    private String shipperUsername; // Lấy từ Account Entity (Shipper)

    // Chi tiết đơn hàng
    private List<OrderItemDTO> items; // Danh sách các sản phẩm trong đơn hàng

    // Thời gian
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}