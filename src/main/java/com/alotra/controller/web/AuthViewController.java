package com.alotra.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * AuthViewController - Handles login and register pages
 */
@Controller
public class AuthViewController {

    /**
     * Login page
     * GET /login
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "redirect", required = false) String redirect,
            Model model) {
        
        // Check if already logged in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/";
        }
        
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng");
        }
        
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        
        model.addAttribute("pageTitle", "Đăng Nhập - AloTra");
        return "login";
    }

    /**
     * Register page
     * GET /register
     */
    @GetMapping("/register")
    public String register(Model model) {
        // Check if already logged in
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/";
        }
        
        model.addAttribute("pageTitle", "Đăng Ký - AloTra");
        return "register";
    }

    /**
     * Logout success page
     * GET /logout-success
     */
    @GetMapping("/logout-success")
    public String logoutSuccess(Model model) {
        model.addAttribute("message", "Đã đăng xuất thành công");
        model.addAttribute("pageTitle", "Đăng Xuất - AloTra");
        return "redirect:/";
    }
}
