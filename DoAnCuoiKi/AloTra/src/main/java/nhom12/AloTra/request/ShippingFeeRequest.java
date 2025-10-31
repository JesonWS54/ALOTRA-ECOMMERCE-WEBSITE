package nhom12.AloTra.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShippingFeeRequest {
    private Integer maChiPhiVC;

    @NotBlank(message = "Tên gói cước không được để trống")
    private String tenGoiCuoc;

    @NotNull(message = "Vui lòng chọn nhà vận chuyển")
    private Integer maNVC;

    @NotBlank(message = "Phương thức vận chuyển không được để trống")
    private String phuongThucVanChuyen;

    @NotNull(message = "Chi phí không được để trống")
    @DecimalMin(value = "0.0", message = "Chi phí không được là số âm")
    private BigDecimal chiPhi;

    @NotNull(message = "Thời gian giao sớm nhất không được để trống")
    @Min(value = 0, message = "Thời gian không được là số âm")
    private Integer ngayGiaoSomNhat;

    @NotNull(message = "Thời gian giao muộn nhất không được để trống")
    @Min(value = 0, message = "Thời gian không được là số âm")
    private Integer ngayGiaoMuonNhat;

    @NotBlank(message = "Vui lòng chọn đơn vị thời gian")
    private String donViThoiGian;

    @NotEmpty(message = "Vui lòng chọn ít nhất một tỉnh thành áp dụng")
    private List<String> cacTinhApDung;
}
