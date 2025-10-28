package AloTra.controller;

import AloTra.Model.ProductDTO;
import AloTra.Model.ReviewDTO;
import AloTra.services.OrderService;
import AloTra.services.ProductService;
import AloTra.services.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional; // Import Optional

@Controller
// @RequestMapping("/user/product") // Bỏ mapping chung nếu đã dùng đường dẫn đầy đủ
public class ProductDetailsController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService; // Inject OrderService

    // --- Giả lập User ID ---
    private static final Long MOCK_USER_ID = 7L; // Giả lập user ID 7
    private static final String COMPLETED_ORDER_STATUS = "COMPLETED"; // Trạng thái đơn hàng hoàn thành

    /**
     * Hiển thị trang chi tiết sản phẩm.
     */
    @GetMapping("/user/product/{id}") // Đường dẫn đầy đủ
    public String viewProductDetails(@PathVariable Long id,
                                     @RequestParam(name = "reviewPage", defaultValue = "0") int reviewPage,
                                     Model model, HttpServletRequest request) {

        // 1. Lấy chi tiết sản phẩm
        // *** SỬA LỖI Ở ĐÂY: Xử lý Optional ***
        Optional<ProductDTO> productOpt = productService.getProductDetails(id);

        if (productOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + id);
            // Có thể thêm cartItemCount nếu cần hiển thị header/nav trên trang lỗi
            return "user/product-details"; // Vẫn trả về view nhưng với thông báo lỗi
        }
        ProductDTO product = productOpt.get(); // Lấy product nếu có
        model.addAttribute("product", product);

        // 2. Lấy đánh giá (phân trang)
        Pageable reviewPageable = PageRequest.of(reviewPage, 5); // Hiển thị 5 đánh giá mỗi trang
        Page<ReviewDTO> reviews = reviewService.getReviewsByProduct(id, reviewPageable);
        model.addAttribute("reviewsPage", reviews);

        // 3. Kiểm tra xem user đã mua hàng chưa
        boolean hasPurchased = orderService.checkUserHasPurchasedProduct(MOCK_USER_ID, id);
        model.addAttribute("hasPurchased", hasPurchased);

        // 4. Gửi thông tin cần thiết khác
        model.addAttribute("currentUri", request.getServletPath());
        // model.addAttribute("cartItemCount", cartService.getCartItemCount(MOCK_USER_ID)); // Cần inject CartService

        return "user/product-details"; // Trả về view
    }

    /**
     * Xử lý submit đánh giá mới.
     */
    @PostMapping("/user/product/{id}/review") // Đường dẫn đầy đủ
    public String submitReview(@PathVariable Long id,
                               @RequestParam Integer rating,
                               @RequestParam String comment,
                               @RequestParam(required = false) List<MultipartFile> mediaFiles,
                               RedirectAttributes redirectAttributes) {

        try {
            // Gọi service để thêm review
            reviewService.addReview(MOCK_USER_ID, id, rating, comment, mediaFiles);
            redirectAttributes.addFlashAttribute("reviewSuccessMessage", "Gửi đánh giá thành công!");
        } catch (RuntimeException e) {
            // Bắt lỗi cụ thể hơn nếu cần (vd: chưa mua hàng, đã đánh giá...)
             System.err.println("Lỗi khi gửi đánh giá: " + e.getMessage()); // Log lỗi
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "Lỗi khi gửi đánh giá: " + e.getMessage());
        } catch (IOException e) {
             System.err.println("Lỗi IO khi upload ảnh/video đánh giá: " + e.getMessage());
            redirectAttributes.addFlashAttribute("reviewErrorMessage", "Lỗi khi xử lý file đính kèm.");
        } catch (Exception e) {
             System.err.println("Lỗi không xác định khi gửi đánh giá: " + e.getMessage());
              redirectAttributes.addFlashAttribute("reviewErrorMessage", "Đã xảy ra lỗi không mong muốn.");
        }

        // Chuyển hướng về trang chi tiết sản phẩm
        return "redirect:/user/product/" + id;
    }
}