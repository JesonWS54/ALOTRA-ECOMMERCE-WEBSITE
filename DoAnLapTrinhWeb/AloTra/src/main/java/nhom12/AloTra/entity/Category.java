package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "DanhMuc")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDanhMuc;
    private String tenDanhMuc;
    private String hinhAnh;
    private boolean kichHoat;
}
