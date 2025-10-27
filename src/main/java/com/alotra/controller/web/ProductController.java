package com.alotra.controller.web;

import com.alotra.entity.Product;
import com.alotra.entity.Category;
import com.alotra.entity.Review;
import com.alotra.service.ProductService;
import com.alotra.service.CategoryService;
import com.alotra.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * ProductController - Handles product listing and detail pages
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ReviewService reviewService;

    /**
     * Products listing page
     * GET /products
     */
    @GetMapping
    public String products(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "sort", defaultValue = "name") String sortBy,
            Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
            Page<Product> products;
            
            // Filter by category or search
            if (categoryId != null) {
                products = productService.getActiveProductsByCategory(categoryId, pageable);
                Optional<Category> category = categoryService.getCategoryById(categoryId);
                category.ifPresent(c -> model.addAttribute("selectedCategory", c));
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchActiveProducts(keyword, pageable);
                model.addAttribute("searchKeyword", keyword);
            } else {
                products = productService.getActiveProducts(pageable);
            }
            
            // Get all categories for filter
            List<Category> categories = categoryService.getAllCategories();
            
            // Add to model
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("pageTitle", "Sản Phẩm - AloTra");
            
            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách sản phẩm");
            return "error";
        }
    }

    /**
     * Product detail page
     * GET /products/{slug}
     */
    @GetMapping("/{slug}")
    public String productDetail(@PathVariable String slug, Model model) {
        try {
            // Get product by slug
            Optional<Product> productOpt = productService.getProductBySlug(slug);
            
            if (productOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy sản phẩm");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Get reviews for this product
            Pageable reviewPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
            Page<Review> reviews = reviewService.getReviewsByProductId(product.getId(), reviewPageable);
            
            // Get related products (same category)
            Pageable relatedPageable = PageRequest.of(0, 4);
            Page<Product> relatedProducts = productService.getActiveProductsByCategory(
                product.getCategory().getId(), relatedPageable);
            
            // Add to model
            model.addAttribute("product", product);
            model.addAttribute("reviews", reviews.getContent());
            model.addAttribute("relatedProducts", relatedProducts.getContent());
            model.addAttribute("pageTitle", product.getName() + " - AloTra");
            
            return "product-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải chi tiết sản phẩm");
            return "error";
        }
    }

    /**
     * Products by category
     * GET /products/category/{slug}
     */
    @GetMapping("/category/{slug}")
    public String productsByCategory(
            @PathVariable String slug,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        
        try {
            Optional<Category> categoryOpt = categoryService.getCategoryBySlug(slug);
            
            if (categoryOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy danh mục");
                return "error";
            }
            
            Category category = categoryOpt.get();
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.getActiveProductsByCategory(category.getId(), pageable);
            
            // Get all categories for navigation
            List<Category> categories = categoryService.getAllCategories();
            
            model.addAttribute("products", products);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("categories", categories);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("pageTitle", category.getName() + " - AloTra");
            
            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải sản phẩm theo danh mục");
            return "error";
        }
    }

    /**
     * Search products
     * GET /products/search
     */
    @GetMapping("/search")
    public String searchProducts(
            @RequestParam("q") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            Model model) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = productService.searchActiveProducts(keyword, pageable);
            
            List<Category> categories = categoryService.getAllCategories();
            
            model.addAttribute("products", products);
            model.addAttribute("categories", categories);
            model.addAttribute("searchKeyword", keyword);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", products.getTotalPages());
            model.addAttribute("pageTitle", "Tìm kiếm: " + keyword + " - AloTra");
            
            return "products";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tìm kiếm sản phẩm");
            return "error";
        }
    }
}
