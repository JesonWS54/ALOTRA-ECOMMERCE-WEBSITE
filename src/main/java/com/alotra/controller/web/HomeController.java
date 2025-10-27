package com.alotra.controller.web;

import com.alotra.entity.Product;
import com.alotra.entity.Category;
import com.alotra.service.ProductService;
import com.alotra.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * HomeController - Handles homepage and main navigation
 */
@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Homepage
     * GET /
     */
    @GetMapping
    public String home(Model model) {
        try {
            // Get featured products (bestsellers)
            Pageable bestsellerPage = PageRequest.of(0, 8);
            Page<Product> bestsellers = productService.getBestsellerProducts(bestsellerPage);
            
            // Get newest products
            Pageable newestPage = PageRequest.of(0, 8);
            Page<Product> newestProducts = productService.getNewestProducts(newestPage);
            
            // Get all categories for navigation
            List<Category> categories = categoryService.getAllCategories();
            
            // Add to model
            model.addAttribute("bestsellers", bestsellers.getContent());
            model.addAttribute("newestProducts", newestProducts.getContent());
            model.addAttribute("categories", categories);
            model.addAttribute("pageTitle", "AloTra - Trà Sữa Ngon Mỗi Ngày");
            
            return "home";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải trang chủ");
            return "error";
        }
    }

    /**
     * About page
     * GET /about
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "Về Chúng Tôi - AloTra");
        return "about";
    }

    /**
     * Contact page
     * GET /contact
     */
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("pageTitle", "Liên Hệ - AloTra");
        return "contact";
    }
}
