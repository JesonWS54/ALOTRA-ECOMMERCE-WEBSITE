package nhom12.AloTra.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ImportDetailRequest {
    @NotNull(message = "Vui lòng chọn sản phẩm")
    private Integer maSanPham;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int soLuong;

    @NotNull(message = "Giá nhập không được để trống")
    @DecimalMin(value = "1", inclusive = true, message = "Giá nhập phải lớn hơn 0")
    private BigDecimal giaNhap;
}
