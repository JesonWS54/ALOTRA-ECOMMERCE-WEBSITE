package nhom12.AloTra.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipTierRequest {
    private Integer maHangThanhVien;

    @NotBlank(message = "Tên hạng không được để trống")
    @Size(max = 50, message = "Tên hạng không được vượt quá 50 ký tự")
    private String tenHang;

    @NotNull(message = "Điểm tối thiểu không được để trống")
    @Min(value = 0, message = "Điểm tối thiểu không được là số âm")
    private Integer diemToiThieu;

    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @DecimalMin(value = "0.0", message = "Tỷ lệ giảm giá không được âm")
    @DecimalMax(value = "100.0", message = "Tỷ lệ giảm giá không được vượt quá 100")
    private BigDecimal phanTramGiamGia;
}
