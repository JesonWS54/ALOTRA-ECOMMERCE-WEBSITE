package nhom12.AloTra.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp, String purpose);
}