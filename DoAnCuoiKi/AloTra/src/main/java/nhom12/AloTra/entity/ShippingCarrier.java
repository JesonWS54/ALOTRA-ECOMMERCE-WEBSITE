package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "NhaVanChuyen")
public class ShippingCarrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNVC;
    private String tenNVC;
    private String soDienThoai;
    private String website;
}