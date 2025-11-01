package nhom12.AloTra.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class AppliedProvinceId implements Serializable {

    @Column(name = "MaChiPhiVC")
    private Integer maChiPhiVC;

    @Column(name = "TenTinhThanh", length = 100)
    private String tenTinhThanh;
}
