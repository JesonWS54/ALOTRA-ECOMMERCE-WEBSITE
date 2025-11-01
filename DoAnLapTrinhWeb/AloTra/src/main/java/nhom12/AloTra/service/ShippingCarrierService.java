package nhom12.AloTra.service;

import nhom12.AloTra.entity.ShippingCarrier;
import nhom12.AloTra.request.ShippingCarrierRequest;

import org.springframework.data.domain.Page;

public interface ShippingCarrierService {
    Page<ShippingCarrier> search(String keyword, int page, int size);

    ShippingCarrier findById(int id);

    void save(ShippingCarrierRequest request);
    void delete(int id);
}
