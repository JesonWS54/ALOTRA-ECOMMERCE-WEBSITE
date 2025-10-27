package com.alotra.controller.web;

import com.alotra.entity.Cart;
import com.alotra.entity.CartItem;
import com.alotra.entity.User;
import com.alotra.service.CartService;
import com.alotra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * CartController - Handles shopping cart page
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    /**
     * View cart page
     * GET /cart
     */
    @GetMapping
    public String viewCart(Model model) {
        try {
            // Get current logged in user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                // Guest user - redirect to login
                return "redirect:/login?redirect=/cart";
            }
            
            String username = auth.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy thông tin người dùng");
                return "error";
            }
            
            User user = userOpt.get();
            
            // Get cart items
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            BigDecimal totalAmount = cartService.calculateTotal(user.getId());
            int totalQuantity = cartService.getTotalQuantity(user.getId());
            
            // Add to model
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("totalQuantity", totalQuantity);
            model.addAttribute("isEmpty", cartItems.isEmpty());
            model.addAttribute("pageTitle", "Giỏ Hàng - AloTra");
            
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải giỏ hàng");
            return "error";
        }
    }

    /**
     * Checkout page
     * GET /cart/checkout
     */
    @GetMapping("/checkout")
    public String checkout(Model model) {
        try {
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login?redirect=/cart/checkout";
            }
            
            String username = auth.getName();
            Optional<User> userOpt = userService.getUserByUsername(username);
            
            if (userOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy thông tin người dùng");
                return "error";
            }
            
            User user = userOpt.get();
            
            // Validate cart
            if (cartService.isCartEmpty(user.getId())) {
                return "redirect:/cart";
            }
            
            // Get cart info
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            BigDecimal totalAmount = cartService.calculateTotal(user.getId());
            
            // Add to model
            model.addAttribute("user", user);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            model.addAttribute("pageTitle", "Thanh Toán - AloTra");
            
            return "checkout";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi thanh toán");
            return "error";
        }
    }
}
