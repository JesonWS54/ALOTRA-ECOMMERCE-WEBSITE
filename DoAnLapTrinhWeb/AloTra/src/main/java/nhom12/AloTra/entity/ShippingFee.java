package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PhiVanChuyen")
public class ShippingFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiPhiVC")
    private Integer maChiPhiVC;

    @Column(name = "TenGoiCuoc", nullable = false, length = 200)
    private String tenGoiCuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNVC", nullable = false)
    private ShippingCarrier nhaVanChuyen;

    @Column(name = "PhuongThucVanChuyen", nullable = false, length = 50)
    private String phuongThucVanChuyen;

    @Column(name = "ChiPhi", nullable = false, precision = 18, scale = 2)
    private BigDecimal chiPhi;

    @Column(name = "NgayGiaoSomNhat", nullable = false)
    private Integer ngayGiaoSomNhat;

    @Column(name = "NgayGiaoMuonNhat", nullable = false)
    private Integer ngayGiaoMuonNhat;

    @Column(name = "DonViThoiGian", nullable = false, length = 20)
    private String donViThoiGian;

    @OneToMany(mappedBy = "phiVanChuyen", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<AppliedProvince> cacTinhApDung = new HashSet<>();
}
