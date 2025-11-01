package nhom12.AloTra.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CartId implements Serializable {
    private Integer nguoiDung;
    private Integer sanPham;
}
