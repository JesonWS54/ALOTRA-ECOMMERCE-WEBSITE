package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "DangKyTamThoi")
public class TemporaryRegister {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDangKy")
    private Integer maDangKy;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "TenDangNhap", nullable = false)
    private String tenDangNhap;

    @Column(name = "MatKhau", nullable = false)
    private String matKhau;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "HetHanLuc", nullable = false)
    private LocalDateTime hetHanLuc;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }
}