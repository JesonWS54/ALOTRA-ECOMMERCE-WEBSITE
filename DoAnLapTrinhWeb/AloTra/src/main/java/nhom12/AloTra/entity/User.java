package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "NguoiDung")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;

    @Column(name = "Email")
    private String email;

    @Column(name = "TenDangNhap")
    private String tenDangNhap;

    @Column(name = "MatKhau")
    private String matKhau;

    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "TrangThai")
    private Integer trangThai;

    @Column(name = "AnhDaiDien")
    private String anhDaiDien;

    @Column(name = "NgayTao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "XacThucEmail")
    private Boolean xacThucEmail = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "MaVaiTro")
    private Role vaiTro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaHangThanhVien")
    private MembershipTier hangThanhVien;

    @Column(name = "DiemTichLuy", nullable = false)
    private Integer diemTichLuy = 0;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDateTime.now();
    }
}