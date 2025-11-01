package nhom12.AloTra.controller;

import nhom12.AloTra.entity.Cart;
import nhom12.AloTra.entity.User;
import nhom12.AloTra.repository.UserRepository;
import nhom12.AloTra.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;
    @Autowired
    private UserRepository nguoiDungRepository;

    @ModelAttribute("globalCartItems")
    public List<Cart> getGlobalCartItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            try {
                return cartService.getCartItems();
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
    @ModelAttribute("globalCurrentUser")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return nguoiDungRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}