package nhom17.OneShop.service.impl;

import nhom17.OneShop.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from-address}")
    private String fromAddress;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Override
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            
            if (purpose.equals("Đăng ký")) {
                message.setSubject("Xác thực tài khoản OneShop");
                message.setText(
                    "Xin chào,\n\n" +
                    "Cảm ơn bạn đã đăng ký tài khoản tại OneShop!\n\n" +
                    "Mã OTP của bạn là: " + otp + "\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "OneShop Team"
                );
            } else if (purpose.equals("Quên mật khẩu")) {
                message.setSubject("Khôi phục mật khẩu OneShop");
                message.setText(
                    "Xin chào,\n\n" +
                    "Bạn đã yêu cầu khôi phục mật khẩu tài khoản OneShop.\n\n" +
                    "Mã OTP của bạn là: " + otp + "\n\n" +
                    "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                    "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.\n\n" +
                    "Trân trọng,\n" +
                    "OneShop Team"
                );
            }
            
            mailSender.send(message);
            System.out.println("✅ Đã gửi email OTP đến: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }
}