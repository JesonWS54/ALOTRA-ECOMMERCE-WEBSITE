package AloTra.services;

import AloTra.Model.CartViewDTO;
import AloTra.Model.VoucherDTO;
import AloTra.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional; // Thêm import

import java.util.Optional;

public interface VoucherService {

    Optional<Voucher> validateAndGetVoucher(String voucherCode, CartViewDTO cartView);
    double calculateDiscount(Voucher voucher, double subtotal);

    // *** VENDOR ***
    Page<VoucherDTO> findShopVouchers(Long shopId, Pageable pageable);
    VoucherDTO createShopVoucher(Long shopId, VoucherDTO voucherDTO);

    // *** THÊM CÁC PHƯƠNG THỨC MỚI ***
    /**
     * Lấy thông tin chi tiết voucher để chỉnh sửa.
     * @param voucherId ID của voucher.
     * @param shopId ID của shop sở hữu (để kiểm tra quyền).
     * @return Optional chứa VoucherDTO nếu tìm thấy và có quyền.
     */
    Optional<VoucherDTO> getShopVoucherForEdit(Long voucherId, Long shopId);

    /**
     * Cập nhật thông tin voucher.
     * @param voucherId ID của voucher cần cập nhật.
     * @param shopId ID của shop sở hữu.
     * @param voucherDTO Dữ liệu voucher mới.
     * @return VoucherDTO đã được cập nhật.
     * @throws RuntimeException Nếu không tìm thấy, không có quyền, hoặc lỗi validation.
     */
    @Transactional // Thêm Transactional
    VoucherDTO updateShopVoucher(Long voucherId, Long shopId, VoucherDTO voucherDTO);

    /**
     * Xóa voucher.
     * @param voucherId ID của voucher cần xóa.
     * @param shopId ID của shop sở hữu.
     * @throws RuntimeException Nếu không tìm thấy, không có quyền, hoặc voucher đang có hiệu lực/đã sử dụng (tùy logic).
     */
    @Transactional // Thêm Transactional
    void deleteShopVoucher(Long voucherId, Long shopId);
}

