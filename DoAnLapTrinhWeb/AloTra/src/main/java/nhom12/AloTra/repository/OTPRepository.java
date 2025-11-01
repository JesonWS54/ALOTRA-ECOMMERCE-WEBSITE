package nhom12.AloTra.repository;

import nhom12.AloTra.entity.OTP;
import nhom12.AloTra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OTPRepository extends JpaRepository<OTP, UUID> {
    
    // Tìm OTP theo mã số và mục đích
    Optional<OTP> findByMaSoAndMucDich(String maSo, String mucDich);
    
    // Tìm OTP chưa sử dụng và chưa hết hạn của người dùng
    Optional<OTP> findByNguoiDungAndMucDichAndDaSuDungFalseAndHetHanLucAfter(
        User nguoiDung,
        String mucDich, 
        LocalDateTime now
    );
    
    // Xóa tất cả OTP cũ của người dùng theo mục đích
    void deleteByNguoiDungAndMucDich(User nguoiDung, String mucDich);
}