package AloTra.services.impl;

import AloTra.Model.CartItemDTO;
import AloTra.Model.CartViewDTO;
import AloTra.Model.OrderDTO;
import AloTra.Model.OrderItemDTO;
import AloTra.Model.VoucherDTO;
import AloTra.entity.*;
import AloTra.repository.*;
import AloTra.services.CartService;
import AloTra.services.OrderService;
import AloTra.services.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private CartService cartService;
    @Autowired private AddressRepository addressRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private VoucherService voucherService;
    @Autowired private VoucherRepository voucherRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private ShippingCarrierRepository shippingCarrierRepository;
    @Autowired private ProductImageRepository productImageRepository;

    private static final double DEFAULT_SHIPPING_COST = 30000.0;

    // --- Giữ nguyên các hàm findUserOrders, checkUserHasPurchasedProduct, createOrdersFromCart, getOrderDetails ---
    @Override
    public Page<OrderDTO> findUserOrders(Long userId, String status, Pageable pageable) {
        Page<Order> orderPage;
        if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            orderPage = orderRepository.findByAccount_IdAndStatus(userId, status, pageable);
        } else {
            orderPage = orderRepository.findByAccount_Id(userId, pageable);
        }
        return orderPage.map(this::convertToDTO);
    }

     @Override
     public Page<OrderDTO> findShopOrders(Long shopId, String status, Pageable pageable) {
         Page<Order> orderPage;
         if (status != null && !status.isEmpty() && !"ALL".equalsIgnoreCase(status)) {
             orderPage = orderRepository.findByShop_IdAndStatus(shopId, status, pageable);
         } else {
             orderPage = orderRepository.findByShop_Id(shopId, pageable);
         }
         return orderPage.map(this::convertToDTO);
     }


    @Override
    public boolean checkUserHasPurchasedProduct(Long userId, Long productId) {
        return orderRepository.checkUserHasPurchasedProduct(userId, productId, "COMPLETED"); // Giả định COMPLETED
    }

     @Override
     @Transactional
     public List<Order> createOrdersFromCart(Long userId, Long addressId, String paymentMethod, String notes, String appliedVoucherCode) {
         CartViewDTO cartView = cartService.getCartView(userId, appliedVoucherCode);
         if (cartView.getItems() == null || cartView.getItems().isEmpty()) { throw new RuntimeException("Giỏ hàng trống."); }
         Addresses shippingAddress = addressRepository.findByIdAndAccount_Id(addressId, userId).orElseThrow(() -> new EntityNotFoundException("Địa chỉ không hợp lệ."));
         Map<Long, List<CartItemDTO>> itemsByShop = cartView.getItems().stream().collect(Collectors.groupingBy(CartItemDTO::getProductShopId));
         Optional<Voucher> voucherOpt = Optional.empty();
         if (appliedVoucherCode != null && !appliedVoucherCode.isEmpty()) { voucherOpt = voucherService.validateAndGetVoucher(appliedVoucherCode, cartView); if (voucherOpt.isEmpty()) { throw new RuntimeException("Mã giảm giá không hợp lệ."); } if (voucherOpt.get().getQuantity() <= voucherOpt.get().getUsedCount()) { throw new RuntimeException("Mã giảm giá đã hết lượt."); } }
         List<Order> createdOrders = new ArrayList<>(); List<Long> cartItemIdsToDelete = new ArrayList<>();
         for (Map.Entry<Long, List<CartItemDTO>> entry : itemsByShop.entrySet()) {
             Long shopId = entry.getKey(); List<CartItemDTO> shopItems = entry.getValue(); if (shopItems.isEmpty()) continue;
             Shop shop = productRepository.findById(shopItems.get(0).getProductId()).map(Product::getShop).orElseThrow(() -> new RuntimeException("Không tìm thấy shop."));
             double itemsTotal = shopItems.stream().mapToDouble(item -> item.getPriceAtAdd() * item.getQuantity()).sum(); double orderDiscount = 0;
             if (voucherOpt.isPresent()) { Voucher voucher = voucherOpt.get(); if (voucher.getShop() == null || voucher.getShop().getId().equals(shopId)) { double shopSubtotal = shopItems.stream().mapToDouble(i -> i.getPriceAtAdd() * i.getQuantity()).sum(); if (shopSubtotal >= (voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : 0.0)) { orderDiscount = voucherService.calculateDiscount(voucher, shopSubtotal); } } }
             double shippingFee = DEFAULT_SHIPPING_COST; double commissionFee = 0; double finalTotal = itemsTotal + shippingFee - orderDiscount; if (finalTotal < 0) finalTotal = 0;
             Order order = new Order(); order.setAccount(accountRepository.findById(userId).orElse(null)); order.setShop(shop); order.setShippingAddress(shippingAddress.getFullAddressText()); order.setShippingPhone(shippingAddress.getPhone()); order.setShippingFullName(shippingAddress.getFullName()); order.setShippingFee(shippingFee); order.setItemsTotalPrice(itemsTotal); order.setVoucherDiscount(orderDiscount); order.setCommissionFee(commissionFee); order.setFinalTotal(finalTotal); order.setStatus("PENDING"); order.setPaymentMethod(paymentMethod); order.setPaymentStatus("UNPAID"); order.setNotes(notes);
             for (CartItemDTO cartItemDTO : shopItems) {
                 Product product = productRepository.findById(cartItemDTO.getProductId()).orElseThrow(() -> new EntityNotFoundException("Sản phẩm ID " + cartItemDTO.getProductId() + " không tồn tại."));
                 if (product.getStockQuantity() < cartItemDTO.getQuantity()) { throw new RuntimeException("Sản phẩm '" + product.getName() + "' không đủ tồn kho."); }
                 OrderItem orderItem = new OrderItem(); orderItem.setProduct(product); orderItem.setProductName(product.getName()); orderItem.setQuantity(cartItemDTO.getQuantity()); orderItem.setPrice(cartItemDTO.getPriceAtAdd()); order.addItem(orderItem);
                 product.setStockQuantity(product.getStockQuantity() - cartItemDTO.getQuantity()); product.setSoldCount((product.getSoldCount() != null ? product.getSoldCount() : 0) + cartItemDTO.getQuantity()); productRepository.save(product);
                 cartItemIdsToDelete.add(cartItemDTO.getId());
             }
             Order savedOrder = orderRepository.save(order); createdOrders.add(savedOrder);
             if (orderDiscount > 0 && voucherOpt.isPresent()) { Voucher voucher = voucherOpt.get(); voucher.setUsedCount((voucher.getUsedCount() != null ? voucher.getUsedCount() : 0) + 1); voucherRepository.save(voucher); }
         }
         if (!cartItemIdsToDelete.isEmpty()) { cartService.clearCartItems(cartItemIdsToDelete); }
         return createdOrders;
     }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderDTO> getOrderDetails(Long orderId, Long userId) {
         Optional<Order> orderOpt = orderRepository.findById(orderId);
         if (orderOpt.isPresent() && orderOpt.get().getAccount() != null && userId.equals(orderOpt.get().getAccount().getId())) {
             OrderDTO dto = convertToDTO(orderOpt.get());
             dto.setItems(orderOpt.get().getOrderItems().stream().map(this::convertItemToDTO).collect(Collectors.toList()));
             return Optional.of(dto);
         }
         return Optional.empty();
    }

    // --- **IMPLEMENT PHƯƠNG THỨC MỚI** ---
    @Override
    public double getTotalRevenueForShop(Long shopId) {
        // Gọi phương thức repository và trả về 0.0 nếu Optional rỗng
        return orderRepository.calculateTotalCompletedRevenueByShop(shopId).orElse(0.0);
    }
    // ------------------------------------

    // --- Helper Methods (convertToDTO, convertItemToDTO, getFirstProductImage giữ nguyên) ---
    private OrderDTO convertToDTO(Order order) {
        if (order == null) return null;
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setAccountId(order.getAccount() != null ? order.getAccount().getId() : null);
        dto.setAccountUsername(order.getAccount() != null ? order.getAccount().getUsername() : null);
        dto.setShopId(order.getShop() != null ? order.getShop().getId() : null);
        dto.setShopName(order.getShop() != null ? order.getShop().getShopName() : null);
        dto.setShippingAddress(order.getShippingAddress());
        dto.setShippingPhone(order.getShippingPhone());
        dto.setShippingFullName(order.getShippingFullName());
        dto.setShippingCarrierId(order.getShippingCarrier() != null ? order.getShippingCarrier().getId() : null);
        dto.setShippingCarrierName(order.getShippingCarrier() != null ? order.getShippingCarrier().getName() : null);
        dto.setShippingFee(order.getShippingFee());
        dto.setItemsTotalPrice(order.getItemsTotalPrice());
        dto.setVoucherDiscount(order.getVoucherDiscount());
        dto.setCommissionFee(order.getCommissionFee());
        dto.setFinalTotal(order.getFinalTotal());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setNotes(order.getNotes());
        dto.setShipperId(order.getShipper() != null ? order.getShipper().getId() : null);
        dto.setShipperUsername(order.getShipper() != null ? order.getShipper().getUsername() : null);
        dto.setItems(new ArrayList<>());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        return dto;
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        if (item == null) return null;
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrder().getId());
        dto.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        dto.setProductName(item.getProductName());
        dto.setProductThumbnail(getFirstProductImage(item.getProduct() != null ? item.getProduct().getId() : null));
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        return dto;
    }

    private String getFirstProductImage(Long productId) {
        if (productId == null) return "https://placehold.co/100x100/eee/ccc?text=N/A";
        return productImageRepository.findByProductIdAndIsThumbnailTrue(productId)
                .map(ProductImage::getImageUrl)
                .orElseGet(() -> productImageRepository.findFirstByProductIdOrderByIdAsc(productId)
                        .map(ProductImage::getImageUrl)
                        .orElse("https://placehold.co/100x100/eee/ccc?text=Img"));
    }
}

