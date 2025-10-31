package nhom12.AloTra.service.impl;

import nhom12.AloTra.entity.Cart;
import nhom12.AloTra.entity.User;
import nhom12.AloTra.entity.Inventory;
import nhom12.AloTra.entity.Product;
import nhom12.AloTra.repository.CartRepository;
import nhom12.AloTra.repository.InventoryRepository;
import nhom12.AloTra.repository.UserRepository;
import nhom12.AloTra.repository.ProductRepository;
import nhom12.AloTra.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired private CartRepository gioHangRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository nguoiDungRepository;
    @Autowired private InventoryRepository inventoryRepository;

    @Override
    @Transactional
    public void addToCart(Integer productId, int quantity) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        Inventory inventory = inventoryRepository.findById(product.getMaSanPham()).orElse(null);
        // Initial quantity check
        if (inventory == null || inventory.getSoLuongTon() < quantity) {
            throw new IllegalStateException(
                    String.format("Không đủ số lượng cho sản phẩm '%s'. Bạn muốn thêm %d, nhưng chỉ còn %d sản phẩm.",
                            product.getTenSanPham(),
                            quantity,
                            inventory != null ? inventory.getSoLuongTon() : 0)
            );
        }

        // Use the repository method that takes User and Product objects
        Optional<Cart> existingCartItemOpt = gioHangRepository.findByNguoiDungAndSanPham(currentUser, product);

        if (existingCartItemOpt.isPresent()) {
            Cart cartItem = existingCartItemOpt.get();
            int newQuantity = cartItem.getSoLuong() + quantity;
            // Check total quantity against inventory
            if (inventory.getSoLuongTon() < newQuantity) {
                throw new IllegalStateException(
                        String.format("Không đủ số lượng cho sản phẩm '%s'. Bạn muốn thêm %d (tổng %d), nhưng chỉ còn %d sản phẩm.",
                                product.getTenSanPham(),
                                quantity,
                                newQuantity,
                                inventory.getSoLuongTon())
                );
            }
            cartItem.setSoLuong(newQuantity);
            gioHangRepository.save(cartItem);
        } else {
            // Quantity already checked at the beginning
            Cart newCartItem = new Cart();
            // No setId needed if JPA manages the composite key via relationships
            newCartItem.setNguoiDung(currentUser);
            newCartItem.setSanPham(product);
            newCartItem.setSoLuong(quantity);
            newCartItem.setDonGia(product.getGiaBan()); // Set current price
            gioHangRepository.save(newCartItem);
        }
    }

    @Override
    public List<Cart> getCartItems() {
        Optional<User> currentUserOpt = getCurrentUserOptional();
        if (currentUserOpt.isPresent()) {
            // Use the repository method that takes a User object
            return gioHangRepository.findByNguoiDungWithProduct(currentUserOpt.get());
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public void updateQuantity(Integer productId, int quantity) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));

        if (quantity < 1) {
            removeItem(productId); // Delegate removal
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        Inventory inventory = inventoryRepository.findById(product.getMaSanPham()).orElse(null);
        if (inventory == null || inventory.getSoLuongTon() < quantity) {
            throw new IllegalStateException(
                    String.format("Không đủ số lượng cho sản phẩm '%s'. Bạn muốn đặt %d, nhưng chỉ còn %d sản phẩm.",
                            product.getTenSanPham(),
                            quantity,
                            inventory != null ? inventory.getSoLuongTon() : 0)
            );
        }
        // Use the repository method that takes User and Product objects
        Cart cartItem = gioHangRepository.findByNguoiDungAndSanPham(currentUser, product)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng."));
        cartItem.setSoLuong(quantity);
        gioHangRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void removeItem(Integer productId) {
        User currentUser = getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm để xóa khỏi giỏ hàng."));
        // Use the repository method that takes User and Product objects
        gioHangRepository.deleteByNguoiDungAndSanPham(currentUser, product);
    }

    @Override
    public BigDecimal getSubtotal() {
        List<Cart> cartItems = getCartItems();
        return cartItems.stream()
                .map(Cart::getThanhTien) // Assumes ThanhTien is correctly calculated in Cart entity
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ===== START: ADD clearCart IMPLEMENTATION =====
    @Override
    @Transactional
    public void clearCart() {
        Optional<User> currentUserOpt = getCurrentUserOptional();
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            // Fetch items belonging to the current user
            List<Cart> itemsToDelete = gioHangRepository.findByNguoiDungWithProduct(currentUser); // Use the method that takes User object
            if (!itemsToDelete.isEmpty()) {
                gioHangRepository.deleteAll(itemsToDelete); // Delete all fetched items
            }
        } else {
            // Log or handle the case where the user isn't logged in
            System.err.println("Attempted to clear cart for non-logged-in user.");
        }
    }

    private Optional<User> getCurrentUserOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }
        String username;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return nguoiDungRepository.findByEmail(username);
    }

    private User getCurrentUser() {
        return getCurrentUserOptional().orElseThrow(() -> new IllegalStateException("Người dùng chưa đăng nhập hoặc không tồn tại."));
    }
}