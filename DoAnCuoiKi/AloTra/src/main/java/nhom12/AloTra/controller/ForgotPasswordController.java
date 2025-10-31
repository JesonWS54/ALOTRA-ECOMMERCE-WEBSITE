package nhom12.AloTra.controller;

import nhom12.AloTra.service.OtpService;
import nhom12.AloTra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    /**
     * Hiển thị trang nhập email để bắt đầu quá trình quên mật khẩu.
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "user/account/forgot-password";
    }

    /**
     * Xử lý việc gửi email, tạo và gửi mã OTP.
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        try {
            userService.sendResetPasswordOtp(email);
            model.addAttribute("email", email);
            return "user/account/verify-reset-password";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    /**
     * Hiển thị trang xác thực OTP.
     */
    @GetMapping("/verify-reset-password")
    public String showVerifyResetPasswordForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "user/account/verify-reset-password";
    }

    /**
     * Xử lý xác thực mã OTP người dùng nhập vào.
     */
    @PostMapping("/verify-reset-password")
    public String verifyResetPasswordOtp(@RequestParam("email") String email,
                                         @RequestParam("otp") String otp,
                                         RedirectAttributes redirectAttributes,
                                         Model model) {
        try {
            boolean isValid = userService.verifyResetPasswordOtp(email, otp);
            if (isValid) {
                model.addAttribute("email", email);
                return "user/account/reset-password";
            } else {
                redirectAttributes.addFlashAttribute("error", "Mã OTP không đúng hoặc đã hết hạn!");
                redirectAttributes.addFlashAttribute("email", email);
                return "redirect:/verify-reset-password?email=" + email;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/verify-reset-password?email=" + email;
        }
    }

    /**
     * Hiển thị trang đặt lại mật khẩu mới (sau khi đã xác thực OTP thành công).
     */
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "user/account/reset-password";
    }

    /**
     * ✅ XỬ LÝ LƯU MẬT KHẨU MỚI
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("email") String email,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirm") String confirm,
                                       RedirectAttributes redirectAttributes) {
        System.out.println("========== XỬ LÝ RESET PASSWORD TỪ FORM ==========");
        System.out.println("📧 Email từ form: " + email);
        System.out.println("🔑 Password từ form: " + password);
        System.out.println("🔑 Confirm từ form: " + confirm);
        
        try {
            if (!password.equals(confirm)) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp!");
            }
            if (password.length() < 6) {
                throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
            }
            
            userService.resetPassword(email, password);
            
            redirectAttributes.addFlashAttribute("success", "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.");
            return "redirect:/sign-in";
        } catch (Exception e) {
            System.err.println("❌ LỖI TRONG CONTROLLER: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/reset-password?email=" + email;
        }
    }

    /**
     * Xử lý yêu cầu gửi lại mã OTP.
     */
    @GetMapping("/resend-reset-otp")
    public String resendResetOtp(@RequestParam("email") String email,
                                 RedirectAttributes redirectAttributes) {
        try {
            otpService.generateOtpForEmail(email, "Quên mật khẩu");
            redirectAttributes.addFlashAttribute("success", "Đã gửi lại mã OTP. Vui lòng kiểm tra email!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể gửi lại OTP. Vui lòng thử lại!");
        }
        return "redirect:/verify-reset-password?email=" + email;
    }
}