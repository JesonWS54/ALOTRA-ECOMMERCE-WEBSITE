package nhom12.AloTra.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PhiApDungTungTinh")
public class AppliedProvince {

    @EmbeddedId
    private AppliedProvinceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("maChiPhiVC")
    @JoinColumn(name = "MaChiPhiVC")
    private ShippingFee phiVanChuyen;
}
