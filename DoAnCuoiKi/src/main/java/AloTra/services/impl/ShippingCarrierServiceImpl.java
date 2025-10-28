package AloTra.services.impl;

import AloTra.Model.ShippingCarrierDTO;
import AloTra.entity.ShippingCarrier;
import AloTra.repository.ShippingCarrierRepository;
import AloTra.services.ShippingCarrierService;
import jakarta.persistence.EntityNotFoundException; // Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShippingCarrierServiceImpl implements ShippingCarrierService {

    @Autowired
    private ShippingCarrierRepository carrierRepository;

    @Override
    public Page<ShippingCarrierDTO> getAllCarriers(Pageable pageable) {
        return carrierRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public List<ShippingCarrierDTO> getActiveCarriers() {
        return carrierRepository.findByIsActiveTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ShippingCarrierDTO> getCarrierById(Long id) {
        return carrierRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    @Transactional
    public ShippingCarrierDTO addCarrier(ShippingCarrierDTO carrierDTO) {
        // Kiểm tra tên trùng (không phân biệt hoa thường)
        if (carrierRepository.existsByNameIgnoreCase(carrierDTO.getName())) {
            throw new RuntimeException("Tên nhà vận chuyển '" + carrierDTO.getName() + "' đã tồn tại.");
        }

        ShippingCarrier carrier = new ShippingCarrier();
        carrier.setName(carrierDTO.getName());
        carrier.setBaseFee(carrierDTO.getBaseFee() != null ? carrierDTO.getBaseFee() : 0.0);
        carrier.setIsActive(carrierDTO.getIsActive() != null ? carrierDTO.getIsActive() : true); // Mặc định là active

        ShippingCarrier savedCarrier = carrierRepository.save(carrier);
        return convertToDTO(savedCarrier);
    }

    @Override
    @Transactional
    public ShippingCarrierDTO updateCarrier(Long id, ShippingCarrierDTO carrierDTO) {
        ShippingCarrier carrier = carrierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy nhà vận chuyển với ID: " + id));

        // Kiểm tra tên trùng (nếu tên thay đổi)
        if (!carrier.getName().equalsIgnoreCase(carrierDTO.getName()) &&
            carrierRepository.existsByNameIgnoreCase(carrierDTO.getName())) {
            throw new RuntimeException("Tên nhà vận chuyển '" + carrierDTO.getName() + "' đã tồn tại.");
        }

        carrier.setName(carrierDTO.getName());
        carrier.setBaseFee(carrierDTO.getBaseFee() != null ? carrierDTO.getBaseFee() : carrier.getBaseFee());
        carrier.setIsActive(carrierDTO.getIsActive() != null ? carrierDTO.getIsActive() : carrier.getIsActive());

        ShippingCarrier updatedCarrier = carrierRepository.save(carrier);
        return convertToDTO(updatedCarrier);
    }

    @Override
    @Transactional
    public void deleteCarrier(Long id) {
        if (!carrierRepository.existsById(id)) {
            throw new EntityNotFoundException("Không tìm thấy nhà vận chuyển với ID: " + id);
        }
        // TODO: Cân nhắc kiểm tra xem nhà vận chuyển này có đang được sử dụng trong đơn hàng nào không trước khi xóa
        carrierRepository.deleteById(id);
    }

    // --- Helper Method ---
    private ShippingCarrierDTO convertToDTO(ShippingCarrier carrier) {
        if (carrier == null) return null;
        ShippingCarrierDTO dto = new ShippingCarrierDTO();
        dto.setId(carrier.getId());
        dto.setName(carrier.getName());
        dto.setBaseFee(carrier.getBaseFee());
        dto.setIsActive(carrier.getIsActive());
        return dto;
    }
}
