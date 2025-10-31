package nhom12.AloTra.service;

import nhom12.AloTra.entity.Voucher;
import nhom12.AloTra.request.VoucherRequest;
import org.springframework.data.domain.Page;

public interface VoucherService {
    Page<Voucher> findAll(String keyword, Integer status, Integer type, int page, int size);
    Voucher findById(String id);
    void save(VoucherRequest request);
    void delete(String id);
}
