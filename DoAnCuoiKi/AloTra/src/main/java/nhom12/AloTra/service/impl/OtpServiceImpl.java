package nhom17.OneShop.service.impl;

import nhom17.OneShop.entity.OTP;
import nhom17.OneShop.repository.OTPRepository;
import nhom17.OneShop.service.EmailService;
import nhom17.OneShop.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OTPRepository maXacThucRepository;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public String generateOtpForEmail(String email, String mucDich) {
        // Tạo mã OTP 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Lưu vào database
        OTP maXacThuc = new OTP();
        maXacThuc.setEmail(email);
        maXacThuc.setMaSo(otp);
        maXacThuc.setMucDich(mucDich);
        maXacThuc.setHetHanLuc(LocalDateTime.now().plusMinutes(5));
        maXacThuc.setDaSuDung(false);

        maXacThucRepository.save(maXacThuc);

        // Gửi email
        emailService.sendOtpEmail(email, otp, mucDich);

        return otp;
    }

    @Override
    @Transactional
    public boolean validateOtp(String email, String otp, String mucDich) {
        // Tìm OTP
        OTP maXacThuc = maXacThucRepository
            .findByMaSoAndMucDich(otp, mucDich)
            .orElse(null);

        if (maXacThuc == null) {
            return false;
        }

        // Kiểm tra email khớp
        if (!email.equals(maXacThuc.getEmail())) {
            return false;
        }

        // Kiểm tra hết hạn
        if (maXacThuc.getHetHanLuc().isBefore(LocalDateTime.now())) {
            return false;
        }

        // Kiểm tra đã sử dụng
        if (maXacThuc.isDaSuDung()) {
            return false;
        }

        // Đánh dấu đã sử dụng
        maXacThuc.setDaSuDung(true);
        maXacThucRepository.save(maXacThuc);

        return true;
    }
}