package nhom12.AloTra.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "SanPham")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maSanPham;
    private String tenSanPham;
    @Lob
    private String moTa;
    private BigDecimal giaBan;
    private BigDecimal giaNiemYet;
    private Integer hanSuDung;
    private String hinhAnh;
    private boolean kichHoat;
    private LocalDateTime ngayTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "MaDanhMuc")
    private Category danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaThuongHieu")
    @JsonIgnore
    private Brand thuongHieu;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Rating> danhSachRating;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Inventory inventory;
}
