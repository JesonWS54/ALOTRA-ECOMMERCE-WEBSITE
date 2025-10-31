package nhom12.AloTra.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String hoTen;
    private String tenDangNhap;
    private String email;
    private String password;
}