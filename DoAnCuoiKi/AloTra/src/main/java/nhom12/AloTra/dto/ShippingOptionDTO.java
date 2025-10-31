package nhom12.AloTra.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShippingOptionDTO {
    private Integer maChiPhiVC;
    private String tenGoiCuoc;
    private BigDecimal chiPhi;
    private Integer ngayGiaoSomNhat;
    private Integer ngayGiaoMuonNhat;
    private String donViThoiGian;
    private String tenNVC;
}