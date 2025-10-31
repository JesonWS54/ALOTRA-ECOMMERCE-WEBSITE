package nhom12.AloTra.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProductRequest {
    private Integer maSanPham;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String tenSanPham;

    private String moTa;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "1", message = "Giá bán phải là số dương")
    private BigDecimal giaBan;

    @NotNull(message = "Giá niêm yết không được để trống")
    @DecimalMin(value = "1", message = "Giá niêm yết phải là số dương")
    private BigDecimal giaNiemYet;

    @Min(value = 0, message = "Hạn sử dụng không được là số âm")
    private int hanSuDung;

    private String hinhAnh;
    private boolean kichHoat = true;

    @NotNull(message = "Vui lòng chọn danh mục")
    private Integer maDanhMuc;

    @NotNull(message = "Vui lòng chọn thương hiệu")
    private Integer maThuongHieu;
}
