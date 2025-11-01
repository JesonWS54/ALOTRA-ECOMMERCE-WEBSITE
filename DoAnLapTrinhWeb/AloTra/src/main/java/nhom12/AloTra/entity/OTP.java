//package nhom12.AloTra.entity;
//
//import jakarta.persistence.*;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Data
//@Entity
//@Table(name = "MaXacThuc")
//public class OTP {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private UUID maOtp;
//    private String maSo;
//    private String mucDich;
//    private LocalDateTime hetHanLuc;
//    private boolean daSuDung;
//    private LocalDateTime ngayTao;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "MaNguoiDung", nullable = false)
//    private User nguoiDung;
//}
package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "MaXacThuc")
public class OTP {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "MaOtp")
    private UUID maOtp;

    @Column(name = "MaSo", nullable = false)
    private String maSo;

    @Column(name = "MucDich")
    private String mucDich;

    @Column(name = "HetHanLuc", nullable = false)
    private LocalDateTime hetHanLuc;

    @Column(name = "DaSuDung")
    private boolean daSuDung = false;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "Email")
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung")
    private User nguoiDung; // Giữ lại để tương thích ngược

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }
}