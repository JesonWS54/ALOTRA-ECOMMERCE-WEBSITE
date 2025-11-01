package nhom12.AloTra.controller;

import nhom12.AloTra.dto.ChatMessageDTO;
import nhom12.AloTra.entity.User;
import nhom12.AloTra.repository.UserRepository;
import nhom12.AloTra.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/init")
    public ResponseEntity<?> initSession(@RequestBody(required = false) Map<String, String> request) {
        try {
            User currentUser = getCurrentUser();
            String tenKhach = request != null ? request.get("tenKhach") : null;
            String emailKhach = request != null ? request.get("emailKhach") : null;
            Integer maNguoiDung = currentUser != null ? currentUser.getMaNguoiDung() : null;

            String sessionId = chatService.getOrCreateSessionId(maNguoiDung, tenKhach, emailKhach);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", sessionId);
            response.put("message", "Session created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String noiDung = request.get("noiDung");

            if (sessionId == null || noiDung == null || noiDung.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
            }

            User currentUser = getCurrentUser();
            Integer maNguoiDung = currentUser != null ? currentUser.getMaNguoiDung() : null;

            ChatMessageDTO message = chatService.sendMessage(sessionId, noiDung, "CUSTOMER", maNguoiDung);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestParam String sessionId) {
        try {
            List<ChatMessageDTO> history = chatService.getChatHistory(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
                String username = ((UserDetails) auth.getPrincipal()).getUsername();
                return userRepository.findByEmail(username).orElse(null);
            }
        } catch (Exception e) {
            // User not logged in, ignore
        }
        return null;
    }
}

