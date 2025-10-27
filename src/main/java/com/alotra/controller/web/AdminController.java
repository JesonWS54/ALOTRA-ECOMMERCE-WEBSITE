package com.alotra.controller.web;

import com.alotra.entity.Order;
import com.alotra.entity.Product;
import com.alotra.entity.User;
import com.alotra.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * AdminController - Handles admin dashboard and management pages
 * Requires ADMIN role
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Check if current user is admin
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Admin Dashboard
     * GET /admin
     */
    @GetMapping
    public String dashboard(Model model) {
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        // Get statistics
        long totalProducts = productService.countAllProducts();
        long totalOrders = orderService.countAllOrders();
        long totalUsers = userService.countAllUsers();
        long pendingOrders = orderService.countOrdersByStatus("PENDING");
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("pageTitle", "Admin Dashboard - AloTra");
        model.addAttribute("activeMenu", "dashboard");
        
        return "admin/dashboard";
    }

    /**
     * Manage Products
     * GET /admin/products
     */
    @GetMapping("/products")
    public String manageProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> products;
        
        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search, pageable);
            model.addAttribute("search", search);
        } else {
            products = productService.getAllProducts(pageable);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("pageTitle", "Quản Lý Sản Phẩm - Admin");
        model.addAttribute("activeMenu", "products");
        
        return "admin/products";
    }

    /**
     * Manage Orders
     * GET /admin/orders
     */
    @GetMapping("/orders")
    public String manageOrders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "status", required = false) String status,
            Model model) {
        
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders;
        
        if (status != null && !status.trim().isEmpty()) {
            orders = orderService.getOrdersByStatus(status, pageable);
            model.addAttribute("selectedStatus", status);
        } else {
            orders = orderService.getAllOrders(pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageTitle", "Quản Lý Đơn Hàng - Admin");
        model.addAttribute("activeMenu", "orders");
        
        return "admin/orders";
    }

    /**
     * Order Detail (Admin)
     * GET /admin/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        Optional<Order> orderOpt = orderService.getOrderById(id);
        
        if (orderOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "error";
        }
        
        Order order = orderOpt.get();
        
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi Tiết Đơn Hàng #" + order.getOrderNumber() + " - Admin");
        model.addAttribute("activeMenu", "orders");
        
        return "admin/order-detail";
    }

    /**
     * Manage Users
     * GET /admin/users
     */
    @GetMapping("/users")
    public String manageUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
        
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users;
        
        if (search != null && !search.trim().isEmpty()) {
            users = userService.searchUsers(search, pageable);
            model.addAttribute("search", search);
        } else {
            users = userService.getAllUsers(pageable);
        }
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("pageTitle", "Quản Lý Người Dùng - Admin");
        model.addAttribute("activeMenu", "users");
        
        return "admin/users";
    }

    /**
     * Product Form (Add/Edit)
     * GET /admin/products/form
     */
    @GetMapping("/products/form")
    public String productForm(
            @RequestParam(value = "id", required = false) Long id,
            Model model) {
        
        if (!isAdmin()) {
            return "redirect:/login";
        }
        
        if (id != null) {
            // Edit mode
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                model.addAttribute("product", productOpt.get());
                model.addAttribute("isEdit", true);
            }
        }
        
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("pageTitle", (id != null ? "Chỉnh Sửa" : "Thêm") + " Sản Phẩm - Admin");
        model.addAttribute("activeMenu", "products");
        
        return "admin/product-form";
    }
}
