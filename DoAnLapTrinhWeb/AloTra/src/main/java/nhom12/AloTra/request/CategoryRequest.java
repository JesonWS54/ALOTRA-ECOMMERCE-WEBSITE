package nhom12.AloTra.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    private Integer maDanhMuc;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 150, message = "Tên danh mục không được vượt quá 150 ký tự")
    private String tenDanhMuc;

    private String hinhAnh;
    private boolean kichHoat;
}
