package AloTra.controller;

import AloTra.Model.ProductDTO; // Sử dụng ProductDTO
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

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserHomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/home")
    public String userHomePage(Model model, HttpServletRequest request) {

        // Tạo Pageable để lấy Top N sản phẩm bán chạy (>10 sold)
        // Ví dụ lấy 12 sản phẩm đầu tiên
        Pageable topSellingPageable = PageRequest.of(0, 12, Sort.by("soldCount").descending());

        Page<ProductDTO> topSellingPage = productService.getTopSellingProductsForHome(topSellingPageable);
        List<ProductDTO> topSellingProducts = topSellingPage.getContent();

        model.addAttribute("topSellingProducts", topSellingProducts);
        model.addAttribute("currentUri", request.getServletPath());

        return "user/home"; // Trỏ về templates/user/home.html
    }
}

