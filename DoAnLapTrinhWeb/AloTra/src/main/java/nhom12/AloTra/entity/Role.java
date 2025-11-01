package nhom12.AloTra.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "VaiTro")
public class Role {
    @Id
    private Integer maVaiTro;
    private String tenVaiTro;
}
