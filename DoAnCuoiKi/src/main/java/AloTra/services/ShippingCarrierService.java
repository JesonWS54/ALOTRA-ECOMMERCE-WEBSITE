package AloTra.services;

import AloTra.Model.ShippingCarrierDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ShippingCarrierService {

    /**
     * Lấy tất cả nhà vận chuyển (có phân trang).
     * @param pageable Thông tin phân trang.
     * @return Trang các ShippingCarrierDTO.
     */
    Page<ShippingCarrierDTO> getAllCarriers(Pageable pageable);

    /**
     * Lấy tất cả nhà vận chuyển đang hoạt động (không phân trang).
     * Dùng cho việc chọn nhà vận chuyển khi tạo đơn hàng chẳng hạn.
     * @return Danh sách ShippingCarrierDTO đang hoạt động.
     */
    List<ShippingCarrierDTO> getActiveCarriers();

    /**
     * Lấy thông tin nhà vận chuyển theo ID.
     * @param id ID nhà vận chuyển.
     * @return Optional chứa ShippingCarrierDTO nếu tìm thấy.
     */
    Optional<ShippingCarrierDTO> getCarrierById(Long id);

    /**
     * Thêm nhà vận chuyển mới.
     * @param carrierDTO Thông tin nhà vận chuyển mới.
     * @return ShippingCarrierDTO đã được tạo.
     * @throws RuntimeException Nếu tên nhà vận chuyển đã tồn tại.
     */
    ShippingCarrierDTO addCarrier(ShippingCarrierDTO carrierDTO);

    /**
     * Cập nhật thông tin nhà vận chuyển.
     * @param id ID nhà vận chuyển cần cập nhật.
     * @param carrierDTO Thông tin mới.
     * @return ShippingCarrierDTO đã được cập nhật.
     * @throws RuntimeException Nếu không tìm thấy nhà vận chuyển hoặc tên mới bị trùng.
     */
    ShippingCarrierDTO updateCarrier(Long id, ShippingCarrierDTO carrierDTO);

    /**
     * Xóa nhà vận chuyển.
     * (Cần cân nhắc: Có nên xóa cứng hay chỉ đánh dấu không hoạt động?)
     * @param id ID nhà vận chuyển cần xóa.
     * @throws RuntimeException Nếu không tìm thấy nhà vận chuyển.
     */
    void deleteCarrier(Long id); // Hoặc có thể là toggleActive(Long id)

}
