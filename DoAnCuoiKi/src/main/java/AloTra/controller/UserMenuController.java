package AloTra.controller;

import AloTra.Model.ProductDTO; // Sử dụng ProductDTO
import AloTra.entity.Category;
import AloTra.services.CategoryService;
import AloTra.services.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional; // Import Optional

@Controller
@RequestMapping("/user")
public class UserMenuController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // User ID giả lập (nếu cần cho chức năng yêu thích sau này)
    // private static final Long MOCK_USER_ID = 7L;

    @GetMapping("/menu")
    public String userMenuPage(Model model, HttpServletRequest request,
                               @RequestParam(name = "category", required = false) Long categoryId,
                               @RequestParam(name = "sort", defaultValue = "newest") String sortType, // Mới nhất là mặc định
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "12") int size) {

        // --- Lấy danh sách Categories cho Sidebar ---
        List<Category> categories = categoryService.getActiveCategories();
        model.addAttribute("categories", categories);

        // --- Tìm tên Category đang được chọn (nếu có) ---
        String selectedCategoryName = "Tất Cả Sản Phẩm"; // Mặc định
        if (categoryId != null && categories != null) {
            Optional<Category> selectedCategoryOpt = categories.stream()
                                                              .filter(cat -> categoryId.equals(cat.getId()))
                                                              .findFirst();
            if (selectedCategoryOpt.isPresent()) {
                selectedCategoryName = selectedCategoryOpt.get().getName();
            } else {
                 selectedCategoryName = "Danh mục không tồn tại"; // Hoặc xử lý khác
            }
        }
        model.addAttribute("selectedCategoryName", selectedCategoryName); // Gửi tên ra view

        // --- Xử lý logic lấy sản phẩm dựa trên sortType ---
        Page<ProductDTO> productPage;
        Pageable pageable;

        if ("bestSelling".equals(sortType)) {
            // Trường hợp đặc biệt: Lấy Top 10 bán chạy nhất
            pageable = PageRequest.of(0, 10); // Chỉ lấy 10 sản phẩm đầu tiên
            productPage = productService.getTop10BestSellingForMenu(pageable);
            model.addAttribute("isBestSellingPage", true);
        } else {
            model.addAttribute("isBestSellingPage", false);
            Sort sort;
            switch (sortType) {
                case "rating":
                    sort = Sort.by("rating").descending();
                    break;
                case "popular":
                    sort = Sort.by("reviewCount").descending();
                    break;
                case "newest":
                default:
                    sort = Sort.by("createdAt").descending();
                    break;
            }
            pageable = PageRequest.of(page, size, sort);
            productPage = productService.getAllActiveProductsForMenu(categoryId, pageable);
        }

        model.addAttribute("productPage", productPage);

        // --- Gửi thông tin phân trang & filter ra view ---
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentSortType", sortType);
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("pageSize", productPage.getSize());
        model.addAttribute("currentUri", request.getServletPath());

        return "user/menu";
    }
}

