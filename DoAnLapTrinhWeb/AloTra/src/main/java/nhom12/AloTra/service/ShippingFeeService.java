package nhom12.AloTra.service;

import nhom12.AloTra.dto.ShippingOptionDTO; // Import DTO
import nhom12.AloTra.entity.ShippingFee;
import nhom12.AloTra.request.ShippingFeeRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ShippingFeeService {
    List<ShippingFee> findAllByProvider(int providerId);
    ShippingFee findById(int id);
    void save(ShippingFeeRequest request);
    void delete(int id);
    Optional<ShippingOptionDTO> findCheapestShippingOption(String province, BigDecimal subtotal);
}