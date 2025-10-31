package nhom12.AloTra.controller;

import nhom12.AloTra.entity.User;
import nhom12.AloTra.entity.Product;
import nhom12.AloTra.entity.WishList;
import nhom12.AloTra.repository.UserRepository;
import nhom12.AloTra.repository.ProductRepository;
import nhom12.AloTra.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;
    @Autowired
    private UserRepository nguoiDungRepository;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/wishlist")
    public String viewWishlist(Model model) {
        User currentUser = getCurrentUser();
        List<WishList> wishlistItems = wishlistRepository.findByNguoiDung(currentUser);
        model.addAttribute("wishlistItems", wishlistItems);
        return "user/shop/wishlist";
    }

    @PostMapping("/wishlist/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<?> toggleWishlist(@PathVariable("productId") Integer productId) {
        try {
            User currentUser = getCurrentUser();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
            Optional<WishList> wishlistItemOpt = wishlistRepository.findByNguoiDungAndSanPham_MaSanPham(currentUser, productId);

            String status;

            if (wishlistItemOpt.isPresent()) {
                wishlistRepository.delete(wishlistItemOpt.get());
                status = "removed";
            } else {
                WishList newItem = new WishList();
                newItem.setNguoiDung(currentUser);
                newItem.setSanPham(product);
                wishlistRepository.save(newItem);
                status = "added";
            }

            long newCount = wishlistRepository.countByNguoiDung(currentUser);
            return ResponseEntity.ok(Map.of("status", status, "count", newCount));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return nguoiDungRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng trong CSDL."));
        }
        throw new IllegalStateException("Người dùng chưa đăng nhập.");
    }
}