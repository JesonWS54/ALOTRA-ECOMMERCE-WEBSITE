package com.alotra.service;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.Product;
import com.alotra.entity.User;
import com.alotra.repository.CartRepository;
import com.alotra.repository.CartItemRepository;
import com.alotra.repository.ProductRepository;
import com.alotra.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * CartService - FINAL VERSION
 * Phù hợp với Entity tiếng Anh
 */
@Service
@Transactional
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    // ==================== CART MANAGEMENT ====================

    /**
     * Lấy hoặc tạo giỏ hàng cho user
     */
    public Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        Optional<Cart> existingCart = cartRepository.findByUser(user);
        
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Tạo giỏ hàng mới
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setCreatedAt(LocalDateTime.now());
        return cartRepository.save(newCart);
    }

    /**
     * Lấy giỏ hàng theo user ID
     */
    public Optional<Cart> getCartByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));
        return cartRepository.findByUser(user);
    }

    /**
     * Lấy giỏ hàng theo user
     */
    public Optional<Cart> getCartByUser(User user) {
        return cartRepository.findByUser(user);
    }

    // ==================== CART ITEM OPERATIONS ====================

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        // Validate input
        if (quantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        // Get or create cart
        Cart cart = getOrCreateCart(userId);

        // Get product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        // Check stock
        if (!productService.isQuantityAvailable(productId, quantity)) {
            throw new RuntimeException("Sản phẩm không đủ số lượng trong kho");
        }

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            // Check stock for new quantity
            if (!productService.isQuantityAvailable(productId, newQuantity)) {
                throw new RuntimeException("Tổng số lượng vượt quá số lượng có sẵn trong kho");
            }

            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Add new item
            cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCreatedAt(LocalDateTime.now());
            cartItem.setUpdatedAt(LocalDateTime.now());
        }

        return cartItemRepository.save(cartItem);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ
     */
    public CartItem updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new RuntimeException("Số lượng phải lớn hơn 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ hàng với ID: " + cartItemId));

        // Check stock
        Long productId = cartItem.getProduct().getId();
        if (!productService.isQuantityAvailable(productId, newQuantity)) {
            throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng có sẵn trong kho");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        return cartItemRepository.save(cartItem);
    }

    /**
     * Tăng số lượng
     */
    public CartItem increaseQuantity(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ hàng với ID: " + cartItemId));

        int newQuantity = cartItem.getQuantity() + 1;

        // Check stock
        Long productId = cartItem.getProduct().getId();
        if (!productService.isQuantityAvailable(productId, newQuantity)) {
            throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng có sẵn trong kho");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        return cartItemRepository.save(cartItem);
    }

    /**
     * Giảm số lượng
     */
    public CartItem decreaseQuantity(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy item trong giỏ hàng với ID: " + cartItemId));

        int newQuantity = cartItem.getQuantity() - 1;

        if (newQuantity <= 0) {
            // Xóa item nếu số lượng = 0
            cartItemRepository.delete(cartItem);
            return null;
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        return cartItemRepository.save(cartItem);
    }

    // ==================== REMOVE OPERATIONS ====================

    /**
     * Xóa item khỏi giỏ hàng
     */
    public void removeFromCart(Long cartItemId) {
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new RuntimeException("Không tìm thấy item trong giỏ hàng với ID: " + cartItemId);
        }
        cartItemRepository.deleteById(cartItemId);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng của user
     */
    public void removeProductFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        cartItemRepository.deleteByCartAndProduct(cart, product);
    }

    /**
     * Xóa toàn bộ giỏ hàng
     */
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.deleteByCart(cart);
    }

    // ==================== CART INFO ====================

    /**
     * Lấy tất cả items trong giỏ hàng
     */
    public List<CartItem> getCartItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.findByCart(cart);
    }

    /**
     * Đếm số items trong giỏ hàng
     */
    public long getCartItemCount(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartItemRepository.countByCart(cart);
    }

    /**
     * Tính tổng số lượng sản phẩm
     */
    public int getTotalQuantity(Long userId) {
        List<CartItem> items = getCartItems(userId);
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Tính tổng tiền trong giỏ hàng
     */
    public BigDecimal calculateTotal(Long userId) {
        List<CartItem> items = getCartItems(userId);
        
        return items.stream()
                .map(item -> {
                    BigDecimal price = item.getProduct().getPrice();
                    int quantity = item.getQuantity();
                    return price.multiply(new BigDecimal(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== VALIDATION ====================

    /**
     * Kiểm tra sản phẩm có trong giỏ hàng không
     */
    public boolean isProductInCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        return cartItemRepository.findByCartAndProduct(cart, product).isPresent();
    }

    /**
     * Validate giỏ hàng (kiểm tra stock)
     */
    public boolean validateCart(Long userId) {
        List<CartItem> items = getCartItems(userId);

        for (CartItem item : items) {
            Long productId = item.getProduct().getId();
            Integer quantity = item.getQuantity();

            if (!productService.isQuantityAvailable(productId, quantity)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Kiểm tra giỏ hàng rỗng
     */
    public boolean isCartEmpty(Long userId) {
        return getCartItemCount(userId) == 0;
    }

    /**
     * Lấy CartItem theo cart và product
     */
    public Optional<CartItem> getCartItem(Cart cart, Product product) {
        return cartItemRepository.findByCartAndProduct(cart, product);
    }
}