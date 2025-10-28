package AloTra.controller;

import AloTra.Model.OrderDTO; // DTO đã có
import AloTra.services.OrderService; // Service đã có
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional; // Import Optional

@Controller
@RequestMapping("/user")
public class OrderDetailController {

    @Autowired
    private OrderService orderService;

    // User ID giả lập (cần đồng bộ với các Controller khác)
    private static final Long MOCK_USER_ID = 7L;

    @GetMapping("/order/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Cần thêm phương thức getOrderDetails vào OrderService
            Optional<OrderDTO> orderOpt = orderService.getOrderDetails(orderId, MOCK_USER_ID);

            if (orderOpt.isPresent()) {
                model.addAttribute("order", orderOpt.get());
                // Lấy thông báo từ redirect (nếu có, vd: sau khi thanh toán)
                if (model.containsAttribute("successMessage")) {
                     model.addAttribute("successMessage", model.getAttribute("successMessage"));
                }
                return "user/order-details"; // Trả về view mới
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng hoặc bạn không có quyền xem.");
                return "redirect:/user/history"; // Chuyển hướng về lịch sử nếu không thấy
            }
        } catch (Exception e) {
            // Log lỗi
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi tải chi tiết đơn hàng.");
            return "redirect:/user/history";
        }
    }
}