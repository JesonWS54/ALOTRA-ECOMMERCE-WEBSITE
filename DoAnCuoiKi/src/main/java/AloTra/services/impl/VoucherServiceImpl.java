package AloTra.services.impl;

import AloTra.Model.CartItemDTO; // Cần CartItemDTO
import AloTra.Model.CartViewDTO;
import AloTra.Model.VoucherDTO; // Import VoucherDTO
import AloTra.entity.Shop; // Import Shop
import AloTra.entity.Voucher;
import AloTra.repository.ShopRepository; // Import ShopRepository
import AloTra.repository.VoucherRepository;
import AloTra.services.VoucherService;
import jakarta.persistence.EntityNotFoundException; // Import EntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set; // Import Set
import java.util.stream.Collectors; // Import Collectors

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private ShopRepository shopRepository; // Inject ShopRepository

    @Override
    public Optional<Voucher> validateAndGetVoucher(String voucherCode, CartViewDTO cartView) {
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập mã voucher.");
        }
        if (cartView == null || cartView.getItems() == null || cartView.getItems().isEmpty()) {
             throw new RuntimeException("Giỏ hàng đang trống, không thể áp dụng voucher.");
        }

        LocalDateTime now = LocalDateTime.now();
        Optional<Voucher> voucherOpt = voucherRepository.findValidVoucherByCode(voucherCode.trim(), now);

        if (voucherOpt.isEmpty()) {
            throw new RuntimeException("Mã voucher không hợp lệ, đã hết hạn hoặc hết lượt sử dụng.");
        }

        Voucher voucher = voucherOpt.get();

        if (cartView.getSubtotal() < voucher.getMinOrderValue()) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu ("
                + String.format("%,.0f", voucher.getMinOrderValue()) + " VNĐ) để áp dụng voucher này.");
        }

        // *** SỬA LỖI: Dùng getShop() ***
        if ("VENDOR".equalsIgnoreCase(voucher.getCreatedByRole()) && voucher.getShop() != null) {
            // *** SỬA LỖI: Dùng getShop() ***
            Long voucherShopId = voucher.getShop().getId();
            Set<Long> shopIdsInCart = cartView.getItems().stream()
                                             .map(CartItemDTO::getProductShopId)
                                             .collect(Collectors.toSet());

            if (shopIdsInCart.size() > 1 || !shopIdsInCart.contains(voucherShopId)) {
                 // *** SỬA LỖI: Dùng getShop() ***
                 throw new RuntimeException("Mã voucher này chỉ áp dụng cho sản phẩm của shop '"
                     + voucher.getShop().getShopName() + "'.");
            }
        }
        return voucherOpt;
    }

    @Override
    public double calculateDiscount(Voucher voucher, double subtotal) {
        double discount = 0;
        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = subtotal * (voucher.getDiscountValue() / 100.0);
            if (voucher.getMaxDiscountAmount() != null && discount > voucher.getMaxDiscountAmount()) {
                discount = voucher.getMaxDiscountAmount();
            }
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountValue();
            if (discount > subtotal) {
                discount = subtotal;
            }
        }
        return discount;
    }

    // --- Phương thức mới cho Vendor ---
    @Override
    public Page<VoucherDTO> findShopVouchers(Long shopId, Pageable pageable) {
        Page<Voucher> voucherPage = voucherRepository.findByShop_Id(shopId, pageable); // Gọi đúng phương thức repo
        return voucherPage.map(this::convertToDTO); // Chuyển đổi sang DTO
    }

    @Override
    @Transactional
    public VoucherDTO createShopVoucher(Long shopId, VoucherDTO voucherDTO) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy shop với ID: " + shopId));

        // Kiểm tra xem code đã tồn tại chưa (trong phạm vi toàn hệ thống hoặc chỉ shop?)
        // Tạm thời kiểm tra toàn hệ thống
        if (voucherRepository.existsByCodeIgnoreCase(voucherDTO.getCode())) {
            throw new RuntimeException("Mã voucher '" + voucherDTO.getCode() + "' đã tồn tại.");
        }

        Voucher voucher = new Voucher();
        voucher.setCode(voucherDTO.getCode().toUpperCase()); // Lưu code in hoa
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setDiscountType(voucherDTO.getDiscountType());
        voucher.setDiscountValue(voucherDTO.getDiscountValue());
        voucher.setMaxDiscountAmount(voucherDTO.getMaxDiscountAmount());
        voucher.setMinOrderValue(voucherDTO.getMinOrderValue() != null ? voucherDTO.getMinOrderValue() : 0.0);
        voucher.setQuantity(voucherDTO.getQuantity());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        voucher.setCreatedByRole("VENDOR"); // Đánh dấu là do Vendor tạo
        // *** SỬA LỖI: Dùng setShop(Shop) ***
        voucher.setShop(shop); // Gán Shop entity
        voucher.setUsedCount(0); // Khởi tạo số lượt đã dùng

        Voucher savedVoucher = voucherRepository.save(voucher);
        return convertToDTO(savedVoucher);
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc
    public Optional<VoucherDTO> getShopVoucherForEdit(Long voucherId, Long shopId) {
        // Tìm voucher và kiểm tra quyền sở hữu
        Optional<Voucher> voucherOpt = voucherRepository.findByIdAndShop_Id(voucherId, shopId);
        return voucherOpt.map(this::convertToDTO); // Chuyển đổi sang DTO nếu tìm thấy
    }

    @Override
    @Transactional
    public VoucherDTO updateShopVoucher(Long voucherId, Long shopId, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findByIdAndShop_Id(voucherId, shopId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher với ID: " + voucherId + " hoặc bạn không có quyền sửa."));

        // Kiểm tra xem code mới (nếu thay đổi) có bị trùng không
        if (!voucher.getCode().equalsIgnoreCase(voucherDTO.getCode()) &&
            voucherRepository.existsByCodeIgnoreCase(voucherDTO.getCode())) {
            throw new RuntimeException("Mã voucher '" + voucherDTO.getCode() + "' đã tồn tại.");
        }

        // Cập nhật các trường cho phép sửa
        voucher.setCode(voucherDTO.getCode().toUpperCase());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setDiscountType(voucherDTO.getDiscountType());
        voucher.setDiscountValue(voucherDTO.getDiscountValue());
        voucher.setMaxDiscountAmount(voucherDTO.getMaxDiscountAmount());
        voucher.setMinOrderValue(voucherDTO.getMinOrderValue() != null ? voucherDTO.getMinOrderValue() : 0.0);
        voucher.setQuantity(voucherDTO.getQuantity());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        // Không cập nhật usedCount, createdByRole, shopId

        Voucher updatedVoucher = voucherRepository.save(voucher);
        return convertToDTO(updatedVoucher);
    }

    @Override
    @Transactional
    public void deleteShopVoucher(Long voucherId, Long shopId) {
        // Kiểm tra sự tồn tại và quyền sở hữu trước khi xóa
        if (!voucherRepository.existsByIdAndShop_Id(voucherId, shopId)) {
             throw new RuntimeException("Không tìm thấy voucher với ID: " + voucherId + " hoặc bạn không có quyền xóa.");
        }
        voucherRepository.deleteById(voucherId);
    }
    // --- Kết thúc phương thức mới ---


    // --- Hàm Helper chuyển đổi Entity sang DTO ---
    private VoucherDTO convertToDTO(Voucher voucher) {
        if (voucher == null) return null;
        VoucherDTO dto = new VoucherDTO();
        dto.setId(voucher.getId());
        dto.setCode(voucher.getCode());
        dto.setDescription(voucher.getDescription());
        dto.setDiscountType(voucher.getDiscountType());
        dto.setDiscountValue(voucher.getDiscountValue());
        dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        dto.setMinOrderValue(voucher.getMinOrderValue());
        dto.setQuantity(voucher.getQuantity());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setStartDate(voucher.getStartDate());
        dto.setEndDate(voucher.getEndDate());
        dto.setCreatedByRole(voucher.getCreatedByRole());
        // *** SỬA LỖI: Dùng getShop() ***
        if (voucher.getShop() != null) {
            // *** SỬA LỖI: Dùng getShop().getId() và getShop().getShopName() ***
            dto.setShopId(voucher.getShop().getId());
            dto.setShopName(voucher.getShop().getShopName()); // Giả định Shop có getShopName()
        }
        return dto;
    }
}

