package nhom12.AloTra.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShippingRequest {
    private Long maVanChuyen;

    @NotNull(message = "Mã đơn hàng không được để trống")
    private Long maDonHang;

    @NotNull(message = "Vui lòng chọn nhà vận chuyển")
    private Integer maNVC;
    private String trangThai;
}
