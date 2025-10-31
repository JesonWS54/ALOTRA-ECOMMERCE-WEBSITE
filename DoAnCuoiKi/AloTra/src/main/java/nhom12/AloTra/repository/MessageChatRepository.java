package nhom17.OneShop.repository;

import nhom17.OneShop.entity.MessageChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageChatRepository extends JpaRepository<MessageChat, Long> {

    List<MessageChat> findByPhienChat_MaPhienChatOrderByThoiGianAsc(String maPhienChat);

    long countByPhienChat_MaPhienChatAndDaXemFalseAndLoaiNguoiGui(String maPhienChat, String loaiNguoiGui);

    @Modifying
    @Query("UPDATE MessageChat t SET t.daXem = true WHERE t.phienChat.maPhienChat = ?1 AND t.loaiNguoiGui = 'CUSTOMER'")
    void markAllAsReadBySessionId(String maPhienChat);

    MessageChat findFirstByPhienChat_MaPhienChatOrderByThoiGianDesc(String maPhienChat);
}
