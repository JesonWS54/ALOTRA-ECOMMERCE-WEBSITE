package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.ShippingCarrier;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.ShippingCarrierRepository;
import nhom17.OneShop.request.ShippingCarrierRequest;
import nhom17.OneShop.service.ShippingCarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ShippingCarrierServiceImpl implements ShippingCarrierService {

    @Autowired
    private ShippingCarrierRepository shippingCarrierRepository;

    @Override
    public Page<ShippingCarrier> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("maNVC").ascending());
        if (StringUtils.hasText(keyword)) {
            return shippingCarrierRepository.findByTenNVCContainingIgnoreCase(keyword, pageable);
        }
        return shippingCarrierRepository.findAll(pageable);
    }

    @Override
    public ShippingCarrier findById(int id) {
        return shippingCarrierRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + id));
    }

    @Override
    @Transactional
    public void save(ShippingCarrierRequest request) {
        validateCarrier(request);
        ShippingCarrier carrier = prepareCarrierEntity(request);
        mapRequestToEntity(request, carrier);
        shippingCarrierRepository.save(carrier);
    }

    private void validateCarrier(ShippingCarrierRequest request) {
        Integer carrierId = request.getMaNVC();
        String carrierName = request.getTenNVC();

        if (carrierId == null) { // Trường hợp Thêm mới
            if (shippingCarrierRepository.existsByTenNVCIgnoreCase(carrierName)) {
                throw new DuplicateRecordException("Tên nhà vận chuyển '" + carrierName + "' đã tồn tại.");
            }
        } else { // Trường hợp Cập nhật
            if (shippingCarrierRepository.existsByTenNVCIgnoreCaseAndMaNVCNot(carrierName, carrierId)) {
                throw new DuplicateRecordException("Tên nhà vận chuyển '" + carrierName + "' đã được sử dụng bởi một nhà vận chuyển khác.");
            }
        }
    }

    private ShippingCarrier prepareCarrierEntity(ShippingCarrierRequest request) {
        if (request.getMaNVC() == null) {
            return new ShippingCarrier();
        }
        return findById(request.getMaNVC());
    }

    private void mapRequestToEntity(ShippingCarrierRequest request, ShippingCarrier carrier) {
        carrier.setTenNVC(request.getTenNVC());
        carrier.setSoDienThoai(request.getSoDienThoai());
        carrier.setWebsite(request.getWebsite());
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (!shippingCarrierRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy nhà vận chuyển để xóa với ID: " + id);
        }

        shippingCarrierRepository.deleteById(id);
    }
}
