package nhom12.AloTra.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandRequest {
    private Integer maThuongHieu;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 150, message = "Tên thương hiệu không được vượt quá 150 ký tự")
    private String tenThuongHieu;
    private String hinhAnh;
    private String moTa;
    private boolean kichHoat;
}
