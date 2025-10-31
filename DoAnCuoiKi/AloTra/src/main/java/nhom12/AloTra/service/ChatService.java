package nhom12.AloTra.service;

import nhom12.AloTra.dto.ChatMessageDTO;
import nhom12.AloTra.dto.ConversationDTO;

import java.util.List;

public interface ChatService {

    String getOrCreateSessionId(Integer maNguoiDung, String tenKhach, String emailKhach);

    /**
     * Gửi tin nhắn
     * @param sessionId - Session ID của phiên chat
     * @param noiDung - Nội dung tin nhắn
     * @param loaiNguoiGui - "CUSTOMER" hoặc "ADMIN"
     * @param maNguoiDung - ID người gửi (null nếu chưa đăng nhập)
     * @return ChatMessageDTO đã lưu
     */
    ChatMessageDTO sendMessage(String sessionId, String noiDung, String loaiNguoiGui, Integer maNguoiDung);

    List<ChatMessageDTO> getChatHistory(String sessionId);

    List<ConversationDTO> getAllConversations();

    List<ConversationDTO> getUnreadConversations();

    void markConversationAsRead(String sessionId);

    void closeConversation(String sessionId);

    long getTotalUnreadCount();
}

