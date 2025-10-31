package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "NhaCungCap")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNCC;
    private String tenNCC;
    private String sdt;
    private String diaChi;
}
