package AloTra.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alotra")
public class WebController {

    // Trang chủ
    @GetMapping
    public String home(Model model) {
        return "web/index"; // Ánh xạ đến templates/web/index.html
    }

    // Trang đăng nhập
    @GetMapping("/login")
    public String login(Model model) {
        return "web/login"; // Ánh xạ đến templates/web/login.html
    }

    // Trang đăng ký
    @GetMapping("/register")
    public String register(Model model) {
        return "web/register"; // Ánh xạ đến templates/web/register.html
    }

}