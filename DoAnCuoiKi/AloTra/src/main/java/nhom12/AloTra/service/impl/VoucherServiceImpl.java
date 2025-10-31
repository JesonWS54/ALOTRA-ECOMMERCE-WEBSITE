package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.Voucher;
import nhom17.OneShop.exception.DuplicateRecordException;
import nhom17.OneShop.exception.NotFoundException;
import nhom17.OneShop.repository.VoucherRepository;
import nhom17.OneShop.request.VoucherRequest;
import nhom17.OneShop.service.VoucherService;
import nhom17.OneShop.specification.VoucherSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public Page<Voucher> findAll(String keyword, Integer status, Integer type, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTao").descending());
        return voucherRepository.findAll(VoucherSpecification.filterByCriteria(keyword, status, type), pageable);
    }

    @Override
    public Voucher findById(String id) {
        return voucherRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(VoucherRequest request) {
        validateVoucher(request);
        Voucher voucher = prepareVoucherEntity(request);
        mapRequestToEntity(request, voucher);
        voucherRepository.save(voucher);
    }

    private void validateVoucher(VoucherRequest request) {
        if (request.getBatDauLuc() != null && request.getKetThucLuc() != null &&
                request.getKetThucLuc().isBefore(request.getBatDauLuc())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        }
    }

    private Voucher prepareVoucherEntity(VoucherRequest request) {
        String voucherCode = request.getMaKhuyenMai().toUpperCase();
        return voucherRepository.findById(voucherCode)
                .orElseGet(() -> { // Chỉ thực thi khi không tìm thấy voucher
                    // Nếu không tìm thấy, kiểm tra xem có voucher nào khác có tên chiến dịch này không (trường hợp tạo mới)
                    if (voucherRepository.existsByTenChienDichIgnoreCase(request.getTenChienDich())) {
                        throw new DuplicateRecordException("Tên chiến dịch '" + request.getTenChienDich() + "' đã tồn tại.");
                    }
                    Voucher newVoucher = new Voucher();
                    newVoucher.setMaKhuyenMai(voucherCode);
                    newVoucher.setNgayTao(LocalDateTime.now());
                    return newVoucher;
                });
    }

    private void mapRequestToEntity(VoucherRequest request, Voucher voucher) {
        voucher.setTenChienDich(request.getTenChienDich());
        voucher.setKieuApDung(request.getKieuApDung());
        voucher.setGiaTri(request.getGiaTri());
        voucher.setBatDauLuc(request.getBatDauLuc());
        voucher.setKetThucLuc(request.getKetThucLuc());
        voucher.setTongTienToiThieu(request.getTongTienToiThieu());
        voucher.setGiamToiDa(request.getGiamToiDa());
        voucher.setGioiHanTongSoLan(request.getGioiHanTongSoLan());
        voucher.setGioiHanMoiNguoi(request.getGioiHanMoiNguoi());
        voucher.setTrangThai(request.getTrangThai());
    }

    @Override
    @Transactional
    public void delete(String id) {
        String voucherCode = id.toUpperCase();
        if (!voucherRepository.existsById(voucherCode)) {
            throw new NotFoundException("Không tìm thấy khuyến mãi để xóa với mã: " + voucherCode);
        }
        voucherRepository.deleteById(voucherCode);
    }
}
