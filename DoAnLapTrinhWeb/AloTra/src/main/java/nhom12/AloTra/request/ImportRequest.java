package nhom12.AloTra.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ImportRequest {
    private Integer maPhieuNhap;

    @NotNull(message = "Vui lòng chọn nhà cung cấp")
    private Integer maNCC;

    @Valid // Kích hoạt validation cho các đối tượng trong list
    @NotEmpty(message = "Phiếu nhập phải có ít nhất một sản phẩm")
    private List<ImportDetailRequest> chiTietPhieuNhapList;
}
