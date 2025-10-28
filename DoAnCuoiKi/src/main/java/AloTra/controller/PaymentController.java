package AloTra.controller;

import AloTra.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest; // Import HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Import Model
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // Import RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import RedirectAttributes

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String fieldName = paramNames.nextElement();
            String fieldValue = request.getParameter(fieldName);
            // Sửa lỗi Query injection ở đây nếu cần (vd: chỉ lấy các param vnp_)
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                params.put(fieldName, fieldValue);
            }
        }

        int result = paymentService.handleVnPayReturn(params);
        String orderIdStr = params.get("vnp_TxnRef") != null ? params.get("vnp_TxnRef").split("_")[0] : null;

        String message;
        boolean isSuccess = false;

        switch (result) {
            case 0:
                message = "Giao dịch thành công!";
                isSuccess = true;
                break;
            case 1:
                message = "Lỗi: Không tìm thấy đơn hàng.";
                break;
            case 2:
                message = "Lỗi: Số tiền không hợp lệ.";
                break;
            case 3:
                message = "Lỗi: Đơn hàng đã được thanh toán trước đó.";
                break;
            case 4:
                message = "Lỗi: Chữ ký không hợp lệ.";
                break;
            default:
                message = "Giao dịch thất bại.";
                break;
        }

        // Nếu thành công, redirect đến trang chi tiết đơn hàng
        if (isSuccess && orderIdStr != null) {
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/user/order/" + orderIdStr; // Redirect đến trang chi tiết
        } else {
             // Nếu thất bại, hiển thị trang kết quả lỗi
             model.addAttribute("paymentStatus", "Thất bại");
             model.addAttribute("paymentMessage", message);
             model.addAttribute("orderId", orderIdStr); // Gửi cả orderId để user biết
             return "user/payment-result"; // View hiển thị lỗi
        }
    }

    // (Optional) Xử lý IPN (cần public server và cấu hình security phù hợp)
    // @PostMapping("/vnpay/ipn")
    // public ResponseEntity<String> vnpayIpn(HttpServletRequest request) { ... }
}