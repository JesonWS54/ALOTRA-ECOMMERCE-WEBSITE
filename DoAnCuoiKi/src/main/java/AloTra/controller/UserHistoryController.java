package AloTra.controller;

import AloTra.Model.OrderDTO; // Giả định
import AloTra.Model.ProductHomeDTO;
import AloTra.services.OrderService;
import AloTra.services.ProductFavoriteService;
import AloTra.services.ProductViewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/user/history")
public class UserHistoryController {

    private static final Long MOCK_USER_ID = 7L; // Giả lập user ID = 7
    private static final int DEFAULT_PAGE_SIZE = 12; // Số item mỗi trang

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductFavoriteService favoriteService;

    @Autowired
    private ProductViewService viewService;

    @GetMapping
    public String viewHistory(
            @RequestParam(name = "tab", required = false, defaultValue = "orders") String activeTab,
            @RequestParam(name = "status", required = false) String orderStatus, // Lọc đơn hàng
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            Model model, HttpServletRequest request) {

        model.addAttribute("currentUri", request.getServletPath());
        model.addAttribute("activeTab", activeTab); // Truyền tab đang active ra view

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE);

        // Xử lý dữ liệu cho từng tab
        switch (activeTab) {
            case "wishlist":
                Page<ProductHomeDTO> favoritePage = favoriteService.findUserFavorites(MOCK_USER_ID, pageable);
                model.addAttribute("wishlistPage", favoritePage);
                break;
            case "viewed":
                Page<ProductHomeDTO> viewedPage = viewService.findUserViewedProducts(MOCK_USER_ID, pageable);
                model.addAttribute("viewedPage", viewedPage);
                break;
            case "reviews":
                // TODO: Lấy danh sách đánh giá khi có ReviewService
                model.addAttribute("reviewsPage", Page.empty(pageable)); // Tạm thời trả về trang rỗng
                break;
             case "stats":
                 // TODO: Tính toán và thêm thống kê vào model
                 model.addAttribute("totalOrders", 0); // Ví dụ
                 model.addAttribute("totalSpent", 0.0); // Ví dụ
                 break;
            case "orders":
            default: // Tab mặc định là đơn hàng
                model.addAttribute("currentStatus", orderStatus); // Truyền status lọc ra view
                Page<OrderDTO> orderPage = orderService.findUserOrders(MOCK_USER_ID, orderStatus, pageable);
                model.addAttribute("orderPage", orderPage);

                // Lấy danh sách các trạng thái đơn hàng để hiển thị filter (có thể lấy từ Enum hoặc DB)
                 List<String> orderStatuses = List.of("PENDING", "CONFIRMED", "SHIPPING", "COMPLETED", "CANCELLED", "RETURNED");
                 model.addAttribute("orderStatuses", orderStatuses);
                break;
        }

        return "user/history"; // Trả về view templates/user/history.html
    }

     // TODO: Thêm các @PostMapping để xử lý xóa khỏi wishlist, xóa khỏi viewed (nếu cần)
}