package nhom17.OneShop.repository;

import nhom17.OneShop.entity.SessionChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionChatRepository extends JpaRepository<SessionChat, String> { // Sửa lại: Dùng SessionChat

    List<SessionChat> findAllByOrderByTinNhanCuoiCungDesc();

    List<SessionChat> findBySoTinChuaDocGreaterThanOrderByTinNhanCuoiCungDesc(Integer soTin);

    // Sửa lại: Dùng thuộc tính lồng nhau cho đúng
    Optional<SessionChat> findByNguoiDung_MaNguoiDung(Integer maNguoiDung);
    Optional<SessionChat> findByTenKhach(String tenKhach);
    long countByTrangThai(String trangThai);
}
