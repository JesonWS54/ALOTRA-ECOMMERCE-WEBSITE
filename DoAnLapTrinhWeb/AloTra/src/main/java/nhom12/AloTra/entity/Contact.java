package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "LienHe")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maLienHe;
    private String chuDe;
    @Lob
    private String noiDung;
    private String trangThai;
    private LocalDateTime ngayGui;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung")
    private User nguoiDung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaQuanTriVien")
    private User nhanVienXuLy;
}
