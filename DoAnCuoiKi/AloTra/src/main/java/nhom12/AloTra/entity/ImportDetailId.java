package nhom17.OneShop.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ImportDetailId implements Serializable {
    private Integer phieuNhap; // Tên thuộc tính phải khớp với tên trong Entity
    private Integer sanPham;   // Tên thuộc tính phải khớp với tên trong Entity
}
