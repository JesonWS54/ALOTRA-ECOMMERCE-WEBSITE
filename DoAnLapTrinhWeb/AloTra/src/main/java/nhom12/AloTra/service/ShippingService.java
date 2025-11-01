package nhom12.AloTra.service;

import nhom12.AloTra.entity.Shipping;
import nhom12.AloTra.entity.User;
import nhom12.AloTra.request.ShippingRequest;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface ShippingService {
    Page<Shipping> search(String keyword, Integer carrierId, String status, String shippingMethod, int page, int size);
    Shipping findById(Long id);
    void save(ShippingRequest request);
    void delete(Long id);
}
