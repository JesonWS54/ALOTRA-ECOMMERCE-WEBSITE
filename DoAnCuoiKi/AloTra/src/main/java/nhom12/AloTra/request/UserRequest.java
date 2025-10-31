package nhom12.AloTra.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class UserRequest {
    private Integer maNguoiDung;

    @NotBlank(message = "Email không được để trống")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 100, message = "Tên đăng nhập phải từ 3 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập chỉ chứa chữ cái, số và dấu gạch dưới")
    private String tenDangNhap;

    private String matKhau;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên không được vượt quá 150 ký tự")
    private String hoTen;

    @Pattern(regexp = "^((0|\\+84)(\\s|\\.)?((3[2-9])|(5[689])|(7[06-9])|(8[1-689])|(9[0-46-9]))(\\d)(\\s|\\.)?(\\d{3})(\\s|\\.)?(\\d{3})|1[89]00(\\s|\\.)?\\d{4,6})$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

    private String anhDaiDien;

    @NotNull(message = "Vui lòng chọn vai trò")
    private Integer maVaiTro;

    private Integer maHangThanhVien;

    @AssertTrue(message = "Mật khẩu phải có ít nhất 6 ký tự")
    public boolean isPasswordValid() {
        if (maNguoiDung != null && !StringUtils.hasText(matKhau)) {
            return true;
        }

        if (maNguoiDung == null && !StringUtils.hasText(matKhau)) {
            return false;
        }

        if (StringUtils.hasText(matKhau) && matKhau.length() < 6) {
            return false;
        }
        return true;
    }
}
