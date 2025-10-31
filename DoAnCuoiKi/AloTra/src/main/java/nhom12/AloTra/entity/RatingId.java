package nhom12.AloTra.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RatingId implements Serializable {
    private Integer sanPham;
    private Integer nguoiDung;
}
