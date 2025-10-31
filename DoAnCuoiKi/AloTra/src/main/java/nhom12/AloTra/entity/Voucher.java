package nhom17.OneShop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "KhuyenMai")
public class Voucher {
    @Id
    private String maKhuyenMai;
    private String tenChienDich;
    private Integer kieuApDung;
    private BigDecimal giaTri;
    private LocalDateTime batDauLuc;
    private LocalDateTime ketThucLuc;
    private BigDecimal tongTienToiThieu;
    private BigDecimal giamToiDa;
    private Integer gioiHanTongSoLan;
    private Integer gioiHanMoiNguoi;
    private Integer trangThai;
    private LocalDateTime ngayTao;
}
