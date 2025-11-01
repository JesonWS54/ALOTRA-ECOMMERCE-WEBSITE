package nhom12.AloTra.service.impl;

import nhom12.AloTra.entity.Supplier;
import nhom12.AloTra.exception.DataIntegrityViolationException;
import nhom12.AloTra.exception.DuplicateRecordException;
import nhom12.AloTra.repository.ImportRepository;
import nhom12.AloTra.repository.SupplierRepository;
import nhom12.AloTra.request.SupplierRequest;
import nhom12.AloTra.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired private
    ImportRepository importRepository;

    @Override
    public Page<Supplier> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("maNCC").ascending());
        if (StringUtils.hasText(keyword)) {
            return supplierRepository.findByTenNCCContainingIgnoreCase(keyword, pageable);
        }
        return supplierRepository.findAll(pageable);
    }

    @Override
    public Supplier findById(int id) {
        return supplierRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(SupplierRequest supplierRequest) {
        validateUniqueSupplierName(supplierRequest);
        Supplier supplier = prepareSupplierEntity(supplierRequest);
        mapRequestToEntity(supplierRequest, supplier);
        supplierRepository.save(supplier);
    }

    private void validateUniqueSupplierName(SupplierRequest request) {
        if (request.getMaNCC() == null) { // Tạo mới
            if (supplierRepository.existsByTenNCCIgnoreCase(request.getTenNCC())) {
                throw new DuplicateRecordException("Tên nhà cung cấp '" + request.getTenNCC() + "' đã tồn tại.");
            }
        } else { // Cập nhật
            if (supplierRepository.existsByTenNCCIgnoreCaseAndMaNCCNot(request.getTenNCC(), request.getMaNCC())) {
                throw new DuplicateRecordException("Tên nhà cung cấp '" + request.getTenNCC() + "' đã được sử dụng.");
            }
        }
    }

    private Supplier prepareSupplierEntity(SupplierRequest request) {
        if (request.getMaNCC() != null) {
            return findById(request.getMaNCC());
        }
        return new Supplier();
    }

    private void mapRequestToEntity(SupplierRequest request, Supplier supplier) {
        supplier.setTenNCC(request.getTenNCC());
        supplier.setSdt(request.getSdt());
        supplier.setDiaChi(request.getDiaChi());
    }

    @Override
    @Transactional
    public void delete(int id) {
        Supplier supplierToDelete = findById(id);
        if (importRepository.existsByNhaCungCap_MaNCC(id)) {
            throw new DataIntegrityViolationException("Không thể xóa nhà cung cấp '" + supplierToDelete.getTenNCC() + "' vì đã có phiếu nhập liên quan.");
        }
        supplierRepository.delete(supplierToDelete);
    }
}
