//package nhom17.OneShop.service;
//
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Service;
//
//import java.security.SecureRandom;
//
///**
// * Service chịu trách nhiệm tạo, quản lý và xác thực mã OTP.
// * Service này sử dụng MailService để gửi email chứa mã OTP.
// */
//@Service
//public class OtpService {
//
//    private final MailService mailService;
//    private final SecureRandom secureRandom = new SecureRandom();
//
//    // Các khóa để lưu trữ thông tin trong HttpSession
//    private static final String OTP_KEY = "otp_code";
//    private static final String EMAIL_KEY = "otp_email";
//    private static final String EXPIRATION_KEY = "otp_expiration_time";
//
//    /**
//     * Thời gian hiệu lực của mã OTP (tính bằng giây).
//     */
//    private static final int EXPIRES_IN_SECONDS = 60;
//
//    public OtpService(MailService mailService) {
//        this.mailService = mailService;
//    }
//
//    /**
//     * Tạo mã OTP mới, gửi qua email và lưu thông tin vào session.
//     *
//     * @param email   Địa chỉ email của người nhận.
//     * @param session Đối tượng HttpSession để lưu trữ thông tin OTP.
//     */
//    public void generateAndSend(String email, HttpSession session) {
//        String otp = generateSixDigitOtp();
//        long expirationTime = System.currentTimeMillis() + EXPIRES_IN_SECONDS * 1000L;
//
//        // 1. Gửi email trước. Nếu bước này thất bại, sẽ có exception và không lưu gì vào session.
//        sendOtpByMail(email, otp);
//
//        // 2. Nếu gửi email thành công, lưu thông tin vào session.
//        session.setAttribute(OTP_KEY, otp);
//        session.setAttribute(EMAIL_KEY, email);
//        session.setAttribute(EXPIRATION_KEY, expirationTime);
//    }
//
//    /**
//     * Xác thực mã OTP do người dùng cung cấp.
//     *
//     * @param userOtp Mã OTP từ người dùng.
//     * @param session Đối tượng HttpSession chứa mã OTP đã gửi.
//     * @return true nếu mã OTP hợp lệ và chưa hết hạn, ngược lại trả về false.
//     */
//    public boolean verify(String userOtp, HttpSession session) {
//        if (isExpired(session)) {
//            return false; // Hết hạn thì không cần kiểm tra nữa
//        }
//
//        String storedOtp = (String) session.getAttribute(OTP_KEY);
//        return userOtp != null && userOtp.equals(storedOtp);
//    }
//
//    /**
//     * Kiểm tra xem mã OTP trong session đã hết hạn hay chưa.
//     *
//     * @param session Đối tượng HttpSession.
//     * @return true nếu đã hết hạn hoặc không tồn tại.
//     */
//    public boolean isExpired(HttpSession session) {
//        Long expirationTime = (Long) session.getAttribute(EXPIRATION_KEY);
//        return expirationTime == null || System.currentTimeMillis() > expirationTime;
//    }
//
//    /**
//     * Lấy email đã được lưu trong quá trình gửi OTP.
//     *
//     * @param session Đối tượng HttpSession.
//     * @return Email của người dùng, hoặc null nếu không có.
//     */
//    public String getEmail(HttpSession session) {
//        return (String) session.getAttribute(EMAIL_KEY);
//    }
//
//    /**
//     * Xóa tất cả thông tin OTP khỏi session sau khi đã sử dụng xong.
//     *
//     * @param session Đối tượng HttpSession.
//     */
//    public void clear(HttpSession session) {
//        session.removeAttribute(OTP_KEY);
//        session.removeAttribute(EMAIL_KEY);
//        session.removeAttribute(EXPIRATION_KEY);
//    }
//
//    /**
//     * Trả về thời gian hiệu lực của OTP (để hiển thị cho người dùng).
//     * @return Số giây hiệu lực.
//     */
//    public int getExpireSeconds() {
//        return EXPIRES_IN_SECONDS;
//    }
//
//    // ================== CÁC PHƯƠNG THỨC HỖ TRỢ (PRIVATE) ==================
//
//    /**
//     * Tạo một chuỗi OTP ngẫu nhiên gồm 6 chữ số.
//     * @return Chuỗi OTP.
//     */
//    private String generateSixDigitOtp() {
//        int number = secureRandom.nextInt(1_000_000); // Tạo số từ 0 đến 999999
//        return String.format("%06d", number); // Định dạng thành chuỗi 6 chữ số, ví dụ: 001234
//    }
//
//    /**
//     * Xây dựng nội dung email và gọi MailService để gửi đi.
//     *
//     * @param email Địa chỉ người nhận.
//     * @param otp   Mã OTP cần gửi.
//     */
//    private void sendOtpByMail(String email, String otp) {
//        String subject = "OneShop - Mã xác thực OTP";
//
//        String htmlBody = String.format("""
//            <div style="font-family: Arial, sans-serif; font-size: 14px; line-height: 1.6;">
//              <p>Xin chào,</p>
//              <p>Mã xác thực OTP của bạn để đặt lại mật khẩu là:</p>
//              <p style="font-size: 24px; font-weight: bold; letter-spacing: 3px; color: #1E90FF;">%s</p>
//              <p>Mã này sẽ có hiệu lực trong <b>%d giây</b>.</p>
//              <p>Nếu bạn không yêu cầu hành động này, vui lòng bỏ qua email này.</p>
//              <hr style="border: 0; border-top: 1px solid #eee;" />
//              <p>Trân trọng,<br/><b>Đội ngũ OneShop</b></p>
//            </div>
//            """, otp, EXPIRES_IN_SECONDS);
//
//        mailService.sendHtmlEmail(email, subject, htmlBody);
//    }
//}
package nhom17.OneShop.service;

public interface OtpService {
    String generateOtpForEmail(String email, String mucDich);
    boolean validateOtp(String email, String otp, String mucDich);
}