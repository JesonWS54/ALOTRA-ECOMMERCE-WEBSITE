package nhom17.OneShop.service.impl;

import nhom17.OneShop.dto.ChatMessageDTO;
import nhom17.OneShop.dto.ConversationDTO;
import nhom17.OneShop.entity.SessionChat;
import nhom17.OneShop.entity.MessageChat;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.repository.SessionChatRepository;
import nhom17.OneShop.repository.MessageChatRepository;
import nhom17.OneShop.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    @Autowired
    private SessionChatRepository sessionChatRepository;

    @Autowired
    private MessageChatRepository messageChatRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public String getOrCreateSessionId(Integer maNguoiDung, String tenKhach, String emailKhach) {
        if (maNguoiDung != null) {
            Optional<SessionChat> existingSession = sessionChatRepository.findByNguoiDung_MaNguoiDung(maNguoiDung);
            if (existingSession.isPresent()) {
                if ("Đã đóng".equals(existingSession.get().getTrangThai())) {
                    existingSession.get().setTrangThai("Đang mở");
                    sessionChatRepository.save(existingSession.get());
                }
                return existingSession.get().getMaPhienChat();
            }
        } else if (tenKhach != null && !tenKhach.isBlank()) {
            Optional<SessionChat> existingSession = sessionChatRepository.findByTenKhach(tenKhach);
            if (existingSession.isPresent()) {
                if ("Đã đóng".equals(existingSession.get().getTrangThai())) {
                    existingSession.get().setTrangThai("Đang mở");
                    sessionChatRepository.save(existingSession.get());
                }
                return existingSession.get().getMaPhienChat();
            }
        }

        String sessionId = "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        SessionChat phienChat = new SessionChat();
        phienChat.setMaPhienChat(sessionId);

        if (maNguoiDung != null) {
            userRepository.findById(maNguoiDung).ifPresent(nguoiDung -> {
                phienChat.setNguoiDung(nguoiDung);
                phienChat.setTenKhach(nguoiDung.getHoTen());
                phienChat.setEmailKhach(nguoiDung.getEmail());
            });
        } else {
            phienChat.setTenKhach(tenKhach);
            phienChat.setEmailKhach(emailKhach);
        }

        phienChat.setTinNhanDauTien(LocalDateTime.now());
        phienChat.setTinNhanCuoiCung(LocalDateTime.now());
        phienChat.setTrangThai("Đang mở");
        phienChat.setSoTinChuaDoc(0);
        sessionChatRepository.save(phienChat);
        return sessionId;
    }

    @Override
    public ChatMessageDTO sendMessage(String sessionId, String noiDung, String loaiNguoiGui, Integer maNguoiDung) {
        SessionChat phienChat = sessionChatRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chat với ID: " + sessionId));

        MessageChat tinNhan = new MessageChat();
        tinNhan.setPhienChat(phienChat);
        tinNhan.setNoiDung(noiDung);
        tinNhan.setLoaiNguoiGui(loaiNguoiGui);
        tinNhan.setDaXem(false);

        if (maNguoiDung != null) {
            userRepository.findById(maNguoiDung).ifPresent(tinNhan::setNguoiDung);
        }

        MessageChat saved = messageChatRepository.save(tinNhan);

        phienChat.setTinNhanCuoiCung(saved.getThoiGian());
        if ("CUSTOMER".equals(loaiNguoiGui)) {
            // Thêm kiểm tra null để an toàn
            int currentUnread = phienChat.getSoTinChuaDoc() == null ? 0 : phienChat.getSoTinChuaDoc();
            phienChat.setSoTinChuaDoc(currentUnread + 1);
        }
        sessionChatRepository.save(phienChat);

        return convertToMessageDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(String sessionId) {
        List<MessageChat> messages = messageChatRepository.findByPhienChat_MaPhienChatOrderByThoiGianAsc(sessionId);
        return messages.stream().map(this::convertToMessageDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getAllConversations() {
        return sessionChatRepository.findAllByOrderByTinNhanCuoiCungDesc().stream()
                .map(this::convertToConversationDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUnreadConversations() {
        return sessionChatRepository.findBySoTinChuaDocGreaterThanOrderByTinNhanCuoiCungDesc(0).stream()
                .map(this::convertToConversationDTO).collect(Collectors.toList());
    }

    @Override
    public void markConversationAsRead(String sessionId) {
        messageChatRepository.markAllAsReadBySessionId(sessionId);
        sessionChatRepository.findById(sessionId).ifPresent(phienChat -> {
            phienChat.setSoTinChuaDoc(0);
            sessionChatRepository.save(phienChat);
        });
    }

    @Override
    public void closeConversation(String sessionId) {
        sessionChatRepository.findById(sessionId).ifPresent(phienChat -> {
            phienChat.setTrangThai("Đã đóng");
            sessionChatRepository.save(phienChat);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUnreadCount() {
        return sessionChatRepository.findBySoTinChuaDocGreaterThanOrderByTinNhanCuoiCungDesc(0)
                .stream().mapToInt(session -> session.getSoTinChuaDoc() == null ? 0 : session.getSoTinChuaDoc()).sum();
    }

    private ChatMessageDTO convertToMessageDTO(MessageChat entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMaTinNhan(entity.getMaTinNhan());
        dto.setMaPhienChat(entity.getPhienChat().getMaPhienChat());
        dto.setNoiDung(entity.getNoiDung());
        dto.setLoaiNguoiGui(entity.getLoaiNguoiGui());
        dto.setThoiGian(entity.getThoiGian());
        dto.setDaXem(entity.getDaXem());

        if (entity.getNguoiDung() != null) {
            dto.setTenNguoiGui(entity.getNguoiDung().getHoTen());
        } else if ("ADMIN".equals(entity.getLoaiNguoiGui())) {
            dto.setTenNguoiGui("Admin OneShop");
        } else {
            dto.setTenNguoiGui(entity.getPhienChat().getTenKhach());
        }
        return dto;
    }

    private ConversationDTO convertToConversationDTO(SessionChat entity) {
        ConversationDTO dto = new ConversationDTO();
        dto.setMaPhienChat(entity.getMaPhienChat());
        dto.setTenKhach(entity.getTenKhach());
        dto.setEmailKhach(entity.getEmailKhach());
        dto.setThoiGianCuoi(entity.getTinNhanCuoiCung());
        dto.setSoTinChuaDoc(entity.getSoTinChuaDoc());
        dto.setTrangThai(entity.getTrangThai());

        if (entity.getNguoiDung() != null) {
            dto.setMaNguoiDung(entity.getNguoiDung().getMaNguoiDung());
        }

        MessageChat lastMessage = messageChatRepository.findFirstByPhienChat_MaPhienChatOrderByThoiGianDesc(entity.getMaPhienChat());
        if (lastMessage != null) {
            dto.setTinNhanCuoi(lastMessage.getNoiDung());
        }
        return dto;
    }
}

