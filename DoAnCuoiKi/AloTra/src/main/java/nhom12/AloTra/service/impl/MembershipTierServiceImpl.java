package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.MembershipTier;
import nhom17.OneShop.exception.DataIntegrityViolationException;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.MembershipTierRepository;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.request.MembershipTierRequest;
import nhom17.OneShop.service.MembershipTierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MembershipTierServiceImpl implements MembershipTierService {

    @Autowired
    private MembershipTierRepository membershipTierRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<MembershipTier> findAllSorted() {
        return membershipTierRepository.findAll(Sort.by("diemToiThieu").ascending());
    }

    @Override
    @Transactional
    public void save(MembershipTierRequest request) {
        validateUniqueFields(request);
        MembershipTier tier = prepareTierEntity(request);
        mapRequestToEntity(request, tier);
        membershipTierRepository.save(tier);
    }

    private void validateUniqueFields(MembershipTierRequest request) {
        Integer tierId = request.getMaHangThanhVien();
        if (tierId == null) { // Tạo mới
            if (membershipTierRepository.existsByTenHangIgnoreCase(request.getTenHang())) {
                throw new DuplicateRecordException("Tên hạng '" + request.getTenHang() + "' đã tồn tại.");
            }
            if (membershipTierRepository.existsByDiemToiThieu(request.getDiemToiThieu())) {
                throw new DuplicateRecordException("Điểm tối thiểu '" + request.getDiemToiThieu() + "' đã được sử dụng.");
            }
        } else { // Cập nhật
            if (membershipTierRepository.existsByTenHangIgnoreCaseAndMaHangThanhVienNot(request.getTenHang(), tierId)) {
                throw new DuplicateRecordException("Tên hạng '" + request.getTenHang() + "' đã được sử dụng bởi hạng khác.");
            }
            if (membershipTierRepository.existsByDiemToiThieuAndMaHangThanhVienNot(request.getDiemToiThieu(), tierId)) {
                throw new DuplicateRecordException("Điểm tối thiểu '" + request.getDiemToiThieu() + "' đã được sử dụng cho hạng khác.");
            }
        }
    }

    private MembershipTier prepareTierEntity(MembershipTierRequest request) {
        if (request.getMaHangThanhVien() != null) {
            return membershipTierRepository.findById(request.getMaHangThanhVien())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy hạng thành viên với ID: " + request.getMaHangThanhVien()));
        }
        return new MembershipTier();
    }

    private void mapRequestToEntity(MembershipTierRequest request, MembershipTier tier) {
        tier.setTenHang(request.getTenHang());
        tier.setDiemToiThieu(request.getDiemToiThieu());
        tier.setPhanTramGiamGia(request.getPhanTramGiamGia());
    }

    @Override
    @Transactional
    public void delete(int id) {
        if (!membershipTierRepository.existsById(id)) {
            throw new NotFoundException("Không tìm thấy hạng thành viên để xóa với ID: " + id);
        }
        // Gọi đến UserRepository để kiểm tra
        if (userRepository.existsByHangThanhVien_MaHangThanhVien(id)) {
            throw new DataIntegrityViolationException("Không thể xóa hạng thành viên này vì đang có người dùng thuộc hạng đó.");
        }
        membershipTierRepository.deleteById(id);
    }
}
