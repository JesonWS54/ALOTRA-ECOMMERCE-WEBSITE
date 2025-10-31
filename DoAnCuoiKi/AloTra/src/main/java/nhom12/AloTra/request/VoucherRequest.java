package nhom12.AloTra.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 30, message = "Mã khuyến mãi không được vượt quá 30 ký tự")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã khuyến mãi chỉ chứa chữ hoa và số")
    private String maKhuyenMai;

    @NotBlank(message = "Tên chiến dịch không được để trống")
    @Size(max = 200, message = "Tên chiến dịch không được vượt quá 200 ký tự")
    private String tenChienDich;

    @NotNull(message = "Vui lòng chọn kiểu áp dụng")
    private Integer kieuApDung;

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị phải là số dương")
    private BigDecimal giaTri;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime batDauLuc;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ketThucLuc;

    @Min(value = 0, message = "Đơn hàng tối thiểu không được âm")
    private BigDecimal tongTienToiThieu;

    @Min(value = 0, message = "Giảm tối đa không được âm")
    private BigDecimal giamToiDa;

    @Min(value = 1, message = "Giới hạn phải là số dương")
    private Integer gioiHanTongSoLan;

    @Min(value = 1, message = "Giới hạn phải là số dương")
    private Integer gioiHanMoiNguoi;

    private Integer trangThai = 1;
}
