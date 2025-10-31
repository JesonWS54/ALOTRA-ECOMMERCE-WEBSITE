package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ThuongHieu")
public class Brand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maThuongHieu;
    private String tenThuongHieu;
    private String hinhAnh;
    @Lob
    private String moTa;
    private boolean kichHoat;
}
