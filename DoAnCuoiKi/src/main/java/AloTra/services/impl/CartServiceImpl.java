package AloTra.services.impl;

import AloTra.Model.CartItemDTO;
import AloTra.Model.CartViewDTO;
import AloTra.Model.VoucherDTO; // Import VoucherDTO
import AloTra.entity.*;
import AloTra.repository.*;
import AloTra.services.CartService;
import AloTra.services.VoucherService; // Import VoucherService
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AccountRepository accountRepository; // Cần AccountRepository
    @Autowired
    private ProductImageRepository productImageRepository; // Inject ProductImageRepository
    @Autowired
    private VoucherService voucherService; // Inject VoucherService

    private static final double DEFAULT_SHIPPING_COST = 30000.0; // Phí ship mặc định 30k
    private static final double TAX_RATE = 0.0; // Thuế VAT (0% trong ví dụ này)

    @Override
    public CartViewDTO getCartView(Long userId, String appliedVoucherCode) { // Thêm voucher code
        Cart cart = findOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCart_IdOrderByAddedAtDesc(cart.getId());

        List<CartItemDTO> itemDTOs = items.stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());

        // --- TÍNH TOÁN TỔNG TIỀN ---
        double subtotal = itemDTOs.stream()
                .mapToDouble(item -> item.getPriceAtAdd() * item.getQuantity())
                .sum();

        double shippingCost = (subtotal > 0) ? DEFAULT_SHIPPING_COST : 0.0; // Chỉ tính ship nếu có hàng
        double tax = subtotal * TAX_RATE;
        double discount = 0.0;
        VoucherDTO appliedVoucher = null; // Lưu thông tin voucher đã áp dụng

        // Tính discount nếu có voucher hợp lệ
        if (appliedVoucherCode != null && !appliedVoucherCode.isEmpty()) {
            CartViewDTO tempCartViewForValidation = new CartViewDTO(); // Tạo DTO tạm để validate
            tempCartViewForValidation.setItems(itemDTOs);
            tempCartViewForValidation.setSubtotal(subtotal);
            Optional<Voucher> voucherOpt = voucherService.validateAndGetVoucher(appliedVoucherCode, tempCartViewForValidation);
            if (voucherOpt.isPresent()) {
                discount = voucherService.calculateDiscount(voucherOpt.get(), subtotal);
                // Bạn có thể chuyển đổi Voucher entity sang VoucherDTO nếu cần hiển thị chi tiết
                 appliedVoucher = convertVoucherToDTO(voucherOpt.get()); // Tạo hàm convert nếu cần
            } else {
                 // Nếu voucher không hợp lệ (đã bị xóa khỏi session nhưng code vẫn còn), bỏ qua
                 System.err.println("Applied voucher code '" + appliedVoucherCode + "' is invalid for current cart.");
            }
        }

        double total = subtotal + shippingCost + tax - discount;
        if (total < 0) total = 0; // Đảm bảo tổng không âm

        // --- Tạo CartViewDTO ---
        CartViewDTO cartView = new CartViewDTO();
        cartView.setCartId(cart.getId()); // Thêm cartId
        cartView.setUserId(userId); // Thêm userId
        cartView.setItems(itemDTOs);
        cartView.setSubtotal(subtotal);
        cartView.setShippingCost(shippingCost);
        cartView.setTax(tax);
        cartView.setDiscount(discount);
        cartView.setTotal(total);
        cartView.setTotalItems(itemDTOs.size()); // Tổng số loại sản phẩm
        cartView.setAppliedVoucher(appliedVoucher); // Thêm thông tin voucher

        return cartView;
    }

     // Hàm helper convert Voucher sang VoucherDTO (nếu cần)
     private VoucherDTO convertVoucherToDTO(Voucher voucher) {
         if (voucher == null) return null;
         VoucherDTO dto = new VoucherDTO();
         dto.setId(voucher.getId());
         dto.setCode(voucher.getCode());
         dto.setDescription(voucher.getDescription());
         dto.setDiscountType(voucher.getDiscountType());
         dto.setDiscountValue(voucher.getDiscountValue());
         dto.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
         // Thêm các trường khác nếu cần
         return dto;
     }

    @Override
    @Transactional
    public void addItemToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }
        Cart cart = findOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại."));

        // Kiểm tra tồn kho
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho.");
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId);

        if (existingItemOpt.isPresent()) {
            // Nếu sản phẩm đã có, cập nhật số lượng
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + quantity;
             // Kiểm tra lại tồn kho với số lượng mới
             if (product.getStockQuantity() < newQuantity) {
                 throw new RuntimeException("Không đủ tồn kho để thêm số lượng này cho sản phẩm '" + product.getName() + "'.");
             }
            existingItem.setQuantity(newQuantity);
            existingItem.setPriceAtAdd(product.getBasePrice()); // Cập nhật giá mới nhất
            existingItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(existingItem);
        } else {
            // Nếu sản phẩm chưa có, tạo mới CartItem
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setPriceAtAdd(product.getBasePrice()); // Lưu giá tại thời điểm thêm
            newItem.setAddedAt(LocalDateTime.now());
            cartItemRepository.save(newItem);
        }
        // Cập nhật thời gian update của Cart
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findByIdAndCart_Account_Id(cartItemId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại trong giỏ hàng của bạn."));

        if (quantity <= 0) {
            // Nếu số lượng <= 0, xóa item
            cartItemRepository.delete(cartItem);
        } else {
            Product product = cartItem.getProduct();
             // Kiểm tra tồn kho trước khi cập nhật
            if (product.getStockQuantity() < quantity) {
                 throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho (" + quantity + ").");
            }
            cartItem.setQuantity(quantity);
            cartItem.setPriceAtAdd(product.getBasePrice()); // Cập nhật giá mới nhất
            cartItem.setAddedAt(LocalDateTime.now()); // Có thể cập nhật hoặc giữ nguyên addedAt ban đầu
            cartItemRepository.save(cartItem);
        }
        // Cập nhật thời gian update của Cart
        Cart cart = cartItem.getCart();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(Long userId, Long cartItemId) {
        // Kiểm tra xem item có thuộc user không trước khi xóa
        if (!cartItemRepository.existsByIdAndCart_Account_Id(cartItemId, userId)) {
             throw new EntityNotFoundException("Sản phẩm không tồn tại trong giỏ hàng của bạn.");
        }
        cartItemRepository.deleteById(cartItemId);

        // Cập nhật thời gian update của Cart (nếu cần)
        Cart cart = findOrCreateCart(userId); // Tìm lại cart để cập nhật
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = findOrCreateCart(userId);
        cartItemRepository.deleteByCart_Id(cart.getId());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

     @Override
     @Transactional // Thêm Transactional
     public void clearCartItems(List<Long> cartItemIds) {
         if (cartItemIds != null && !cartItemIds.isEmpty()) {
             cartItemRepository.deleteAllByIdInBatch(cartItemIds); // Xóa hiệu quả hơn
             // Có thể cần cập nhật updatedAt của Cart nếu biết cartId
         }
     }


    @Override
    public int getCartItemCount(Long userId) {
        Cart cart = findOrCreateCart(userId);
        // Đếm số lượng item trong giỏ hàng
        return cartItemRepository.countByCart_Id(cart.getId());
    }

    // --- Helper Methods ---

    // Tìm hoặc tạo giỏ hàng cho user
    private Cart findOrCreateCart(Long userId) {
        return cartRepository.findByAccount_Id(userId).orElseGet(() -> {
            Cart newCart = new Cart();
            Account account = accountRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy tài khoản."));
            newCart.setAccount(account);
            newCart.setCreatedAt(LocalDateTime.now());
            newCart.setUpdatedAt(LocalDateTime.now());
            return cartRepository.save(newCart);
        });
    }

    // Chuyển đổi CartItem sang CartItemDTO
    private CartItemDTO convertItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setCartId(item.getCart().getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setProductThumbnail(getFirstProductImage(item.getProduct().getId())); // Lấy ảnh
        dto.setQuantity(item.getQuantity());
        dto.setPriceAtAdd(item.getPriceAtAdd());
        dto.setAddedAt(item.getAddedAt());
        dto.setProductShopId(item.getProduct().getShop().getId()); // Thêm shop ID
        return dto;
    }

    // Lấy ảnh đầu tiên của sản phẩm
    private String getFirstProductImage(Long productId) {
         if (productId == null) return "https://placehold.co/100x100/eee/ccc?text=N/A";
         // Lấy ảnh từ Product entity (đã được load LAZY nếu cần trong transaction)
         Product product = productRepository.findById(productId).orElse(null);
         if (product == null || product.getImages() == null || product.getImages().isEmpty()) {
             return "https://placehold.co/100x100/eee/ccc?text=Img"; // Ảnh mặc định
         }

         Optional<ProductImage> thumbnail = product.getImages().stream()
                // *** SỬA LỖI: Dùng getter getIsThumbnail() ***
                .filter(ProductImage::getIsThumbnail)
                .findFirst();

         if (thumbnail.isPresent()) {
             return thumbnail.get().getImageUrl();
         } else {
             // Lấy ảnh đầu tiên trong Set nếu không có thumbnail (cần sắp xếp nếu muốn ổn định)
             return product.getImages().iterator().next().getImageUrl();
         }
    }
}

