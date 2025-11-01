package nhom12.AloTra.service;

import nhom12.AloTra.entity.Supplier;
import nhom12.AloTra.request.SupplierRequest;
import org.springframework.data.domain.Page;

public interface SupplierService {
    Page<Supplier> search(String keyword, int page, int size);
    Supplier findById(int id);
    void save(SupplierRequest supplierRequest);
    void delete(int id);
}
