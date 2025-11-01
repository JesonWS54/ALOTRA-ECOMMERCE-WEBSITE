package nhom12.AloTra.service;

import nhom12.AloTra.entity.Cart;
import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    List<Cart> getCartItems();
    void addToCart(Integer productId, int quantity);
    void updateQuantity(Integer productId, int quantity);
    void removeItem(Integer productId);
    BigDecimal getSubtotal();

    void clearCart(); // Method to clear the cart for the current user
}