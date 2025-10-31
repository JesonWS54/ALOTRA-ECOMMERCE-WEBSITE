package nhom17.OneShop.service.impl;

import nhom17.OneShop.dto.ShippingOptionDTO;
import nhom17.OneShop.entity.AppliedProvince;
import nhom17.OneShop.entity.AppliedProvinceId;
import nhom17.OneShop.entity.ShippingCarrier;
import nhom17.OneShop.entity.ShippingFee;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.ShippingCarrierRepository;
import nhom17.OneShop.repository.ShippingFeeRepository;
import nhom17.OneShop.request.ShippingFeeRequest;
import nhom17.OneShop.service.CartService;
import nhom17.OneShop.service.ShippingFeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShippingFeeServiceImpl implements ShippingFeeService {

    @Autowired private ShippingFeeRepository shippingFeeRepository;
    @Autowired private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired private CartService cartService;

    @Override
    public List<ShippingFee> findAllByProvider(int providerId) {
        return shippingFeeRepository.findByNhaVanChuyen_MaNVCOrderByMaChiPhiVCDesc(providerId);
    }

    @Override
    public ShippingFee findById(int id) {
        return shippingFeeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy gói phí vận chuyển với ID: " + id));
    }

    @Override
    @Transactional
    public void save(ShippingFeeRequest request) {
        validateRequest(request);
        ShippingFee entity = prepareEntity(request);
        mapToEntity(request, entity);

        ShippingFee savedEntity = shippingFeeRepository.save(entity); // Lưu để lấy ID

        updateAppliedProvincesPostSave(request, savedEntity); // Cập nhật tỉnh thành sau khi có ID

    }

    private void validateRequest(ShippingFeeRequest request) {
        Integer feeId = request.getMaChiPhiVC();
        Integer carrierId = request.getMaNVC();
        String method = request.getPhuongThucVanChuyen();
        String tenGoiCuocTrimmed = request.getTenGoiCuoc() != null ? request.getTenGoiCuoc().trim() : "";

        if (request.getNgayGiaoMuonNhat() != null && request.getNgayGiaoSomNhat() != null &&
                request.getNgayGiaoMuonNhat() <= request.getNgayGiaoSomNhat()) {
            throw new IllegalArgumentException("Ngày giao muộn nhất không được nhỏ hơn hoặc bằng ngày giao sớm nhất.");
        }

        boolean exists;
        if (feeId == null) {
            exists = shippingFeeRepository.existsByPhuongThucVanChuyenIgnoreCaseAndTenGoiCuocIgnoreCaseAndNhaVanChuyen_MaNVC(
                    method, tenGoiCuocTrimmed, carrierId);
            if(exists) throw new DuplicateRecordException("Gói cước '" + tenGoiCuocTrimmed + "' với phương thức '" + method + "' đã tồn tại cho nhà vận chuyển này.");
        } else {
            exists = shippingFeeRepository.existsByPhuongThucVanChuyenIgnoreCaseAndTenGoiCuocIgnoreCaseAndNhaVanChuyen_MaNVCAndMaChiPhiVCNot(
                    method, tenGoiCuocTrimmed, carrierId, feeId);
            if(exists) throw new DuplicateRecordException("Gói cước '" + tenGoiCuocTrimmed + "' với phương thức '" + method + "' bị trùng với gói cước khác của nhà vận chuyển này.");
        }
        // Có thể thêm lại phần kiểm tra tỉnh trùng lặp ở đây nếu cần thiết và đảm bảo logic đúng
    }

    private ShippingFee prepareEntity(ShippingFeeRequest request) {
        if (request.getMaChiPhiVC() != null) {
            return shippingFeeRepository.findById(request.getMaChiPhiVC())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy gói phí vận chuyển để cập nhật: " + request.getMaChiPhiVC()));
        }
        return new ShippingFee();
    }

    private void mapToEntity(ShippingFeeRequest request, ShippingFee entity) {
        ShippingCarrier provider = shippingCarrierRepository.findById(request.getMaNVC())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy nhà vận chuyển với ID: " + request.getMaNVC()));

        entity.setTenGoiCuoc(request.getTenGoiCuoc().trim());
        entity.setNhaVanChuyen(provider);
        entity.setPhuongThucVanChuyen(request.getPhuongThucVanChuyen());
        entity.setChiPhi(request.getChiPhi());
        entity.setNgayGiaoSomNhat(request.getNgayGiaoSomNhat());
        entity.setNgayGiaoMuonNhat(request.getNgayGiaoMuonNhat());
        entity.setDonViThoiGian(request.getDonViThoiGian());
    }

    private void updateAppliedProvincesPostSave(ShippingFeeRequest request, ShippingFee savedEntity) {
        Set<AppliedProvince> currentProvinces = savedEntity.getCacTinhApDung();
        if (currentProvinces == null) {
            currentProvinces = new HashSet<>();
        }
        Set<String> newProvinceNames = (request.getCacTinhApDung() != null) ? new HashSet<>(request.getCacTinhApDung()) : new HashSet<>();

        boolean changed = currentProvinces.removeIf(p -> !newProvinceNames.contains(p.getId().getTenTinhThanh()));

        Set<String> existingProvinceNames = currentProvinces.stream()
                .map(p -> p.getId().getTenTinhThanh())
                .collect(Collectors.toSet());

        for (String tenTinhThanh : newProvinceNames) {
            if (!existingProvinceNames.contains(tenTinhThanh)) {
                AppliedProvinceId appliedId = new AppliedProvinceId(savedEntity.getMaChiPhiVC(), tenTinhThanh);
                AppliedProvince appliedProvince = new AppliedProvince(appliedId, savedEntity); // Constructor mới nếu có
                currentProvinces.add(appliedProvince);
                changed = true;
            }
        }
        // Chỉ gán lại và lưu nếu có thay đổi
        if (changed) {
            savedEntity.setCacTinhApDung(currentProvinces);
            shippingFeeRepository.save(savedEntity); // Lưu lại để cập nhật collection
        }
    }


    @Override
    @Transactional
    public void delete(int id) {
        if (!shippingFeeRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy gói phí vận chuyển để xóa.");
        }
        shippingFeeRepository.deleteById(id);
    }

    @Override
    public Optional<ShippingOptionDTO> findCheapestShippingOption(String province, BigDecimal subtotal) {
        List<ShippingFee> applicableFees = shippingFeeRepository.findApplicableFeesByProvinceOrderedByCost(province);

        if (applicableFees.isEmpty()) {
            return Optional.empty();
        }

        ShippingFee cheapestFeeEntity = applicableFees.get(0);

        BigDecimal finalCost;
        BigDecimal oneMillion = new BigDecimal("1000000");
        BigDecimal fiveHundredThousand = new BigDecimal("500000");
        BigDecimal originalCost = cheapestFeeEntity.getChiPhi();

        if (subtotal.compareTo(oneMillion) > 0) {
            finalCost = BigDecimal.ZERO;
        } else if (subtotal.compareTo(fiveHundredThousand) > 0) {
            finalCost = originalCost.multiply(new BigDecimal("0.5")).setScale(0, RoundingMode.HALF_UP);
        } else {
            finalCost = originalCost;
        }

        ShippingOptionDTO dto = new ShippingOptionDTO();
        dto.setMaChiPhiVC(cheapestFeeEntity.getMaChiPhiVC());
        dto.setTenGoiCuoc(cheapestFeeEntity.getTenGoiCuoc());
        dto.setChiPhi(finalCost);
        dto.setNgayGiaoSomNhat(cheapestFeeEntity.getNgayGiaoSomNhat());
        dto.setNgayGiaoMuonNhat(cheapestFeeEntity.getNgayGiaoMuonNhat());
        dto.setDonViThoiGian(cheapestFeeEntity.getDonViThoiGian());

        ShippingCarrier carrier = cheapestFeeEntity.getNhaVanChuyen();
        dto.setTenNVC(carrier != null ? carrier.getTenNVC() : "Không xác định");

        return Optional.of(dto);
    }
}