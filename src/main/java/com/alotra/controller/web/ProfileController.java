package com.alotra.controller.web;

import com.alotra.entity.Order;
import com.alotra.entity.User;
import com.alotra.service.OrderService;
import com.alotra.service.UserService;
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
 * ProfileController - Handles user profile and order history
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    /**
     * Get current logged in user
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        
        String username = auth.getName();
        Optional<User> userOpt = userService.getUserByUsername(username);
        return userOpt.orElse(null);
    }

    /**
     * Profile overview
     * GET /profile
     */
    @GetMapping
    public String profile(Model model) {
        User user = getCurrentUser();
        
        if (user == null) {
            return "redirect:/login?redirect=/profile";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Tài Khoản - AloTra");
        model.addAttribute("activeTab", "profile");
        
        return "profile";
    }

    /**
     * Order history
     * GET /profile/orders
     */
    @GetMapping("/orders")
    public String orders(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        User user = getCurrentUser();
        
        if (user == null) {
            return "redirect:/login?redirect=/profile/orders";
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderService.getOrdersByUserId(user.getId(), pageable);
        
        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageTitle", "Đơn Hàng - AloTra");
        model.addAttribute("activeTab", "orders");
        
        return "profile-orders";
    }

    /**
     * Order detail
     * GET /profile/orders/{id}
     */
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        User user = getCurrentUser();
        
        if (user == null) {
            return "redirect:/login?redirect=/profile/orders/" + id;
        }
        
        Optional<Order> orderOpt = orderService.getOrderById(id);
        
        if (orderOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy đơn hàng");
            return "error";
        }
        
        Order order = orderOpt.get();
        
        // Verify order belongs to current user
        if (!order.getUser().getId().equals(user.getId())) {
            model.addAttribute("error", "Bạn không có quyền xem đơn hàng này");
            return "error";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Chi Tiết Đơn Hàng #" + order.getOrderNumber() + " - AloTra");
        model.addAttribute("activeTab", "orders");
        
        return "order-detail";
    }

    /**
     * Edit profile
     * GET /profile/edit
     */
    @GetMapping("/edit")
    public String editProfile(Model model) {
        User user = getCurrentUser();
        
        if (user == null) {
            return "redirect:/login?redirect=/profile/edit";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Chỉnh Sửa Tài Khoản - AloTra");
        model.addAttribute("activeTab", "edit");
        
        return "profile-edit";
    }

    /**
     * Change password
     * GET /profile/change-password
     */
    @GetMapping("/change-password")
    public String changePassword(Model model) {
        User user = getCurrentUser();
        
        if (user == null) {
            return "redirect:/login?redirect=/profile/change-password";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Đổi Mật Khẩu - AloTra");
        model.addAttribute("activeTab", "password");
        
        return "profile-password";
    }
}
