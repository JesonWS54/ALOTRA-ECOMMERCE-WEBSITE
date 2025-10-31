package nhom12.AloTra.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplierRequest {
    private Integer maNCC;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 255, message = "Tên nhà cung cấp không được vượt quá 255 ký tự")
    private String tenNCC;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^((0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})|1[89]00(\\s|\\.)?\\d{4,6})$", message = "Số điện thoại không hợp lệ")
    private String sdt;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChi;
}
