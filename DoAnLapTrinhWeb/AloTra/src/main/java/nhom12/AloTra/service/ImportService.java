package nhom12.AloTra.service;

import nhom17.OneShop.entity.Import;
import nhom17.OneShop.request.ImportRequest;
import org.springframework.data.domain.Page;

public interface ImportService {
    Page<Import> findAll(String keyword, Integer supplierId, int page, int size);
    Import findById(int id);
    void save(ImportRequest phieuNhapRequest);
    void delete(int id);
	void save(nhom12.AloTra.request.ImportRequest importRequest);
}
