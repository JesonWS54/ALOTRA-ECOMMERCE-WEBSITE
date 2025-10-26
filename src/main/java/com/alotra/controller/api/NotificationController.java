package com.alotra.controller.api;

import com.alotra.dto.NotificationMessage;
import com.alotra.dto.NotificationMessage.NotificationType;
import com.alotra.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * NotificationController - REST and WebSocket endpoints for notifications
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Test endpoint to check if notification service is running
     * GET /api/notifications/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Notification service is running!");
        response.put("websocketEndpoint", "ws://localhost:8080/ws");
        response.put("subscriptions", "/topic/notifications, /user/{username}/queue/notifications, /topic/admin/notifications");
        return ResponseEntity.ok(response);
    }

    // ==================== REST ENDPOINTS FOR TESTING ====================

    /**
     * Send notification to all users (broadcast)
     * POST /api/notifications/broadcast
     */
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastNotification(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "Thông báo");
        String message = request.getOrDefault("message", "Nội dung thông báo");
        String typeStr = request.getOrDefault("type", "GENERAL");
        
        NotificationType type;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = NotificationType.GENERAL;
        }

        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .priority("MEDIUM")
                .build();

        notificationService.sendToAll(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification broadcasted to all users");
        response.put("notification", notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Send notification to specific user
     * POST /api/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendToUser(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String title = request.getOrDefault("title", "Thông báo");
        String message = request.getOrDefault("message", "Nội dung thông báo");
        String typeStr = request.getOrDefault("type", "GENERAL");

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }

        NotificationType type;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = NotificationType.GENERAL;
        }

        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .priority("MEDIUM")
                .build();

        notificationService.sendToUser(username, notification);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification sent to user: " + username);
        response.put("notification", notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Send notification to admins
     * POST /api/notifications/admin
     */
    @PostMapping("/admin")
    public ResponseEntity<?> sendToAdmins(@RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "Thông báo Admin");
        String message = request.getOrDefault("message", "Nội dung thông báo");
        String typeStr = request.getOrDefault("type", "SYSTEM_ALERT");

        NotificationType type;
        try {
            type = NotificationType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            type = NotificationType.SYSTEM_ALERT;
        }

        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .priority("HIGH")
                .build();

        notificationService.sendToAdmins(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Notification sent to all admins");
        response.put("notification", notification);

        return ResponseEntity.ok(response);
    }

    /**
     * Test new order notification
     * POST /api/notifications/test/new-order
     */
    @PostMapping("/test/new-order")
    public ResponseEntity<?> testNewOrder(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.getOrDefault("orderId", 123).toString());
        String username = (String) request.getOrDefault("username", "user1");
        String customerName = (String) request.getOrDefault("customerName", "Nguyễn Văn A");

        notificationService.notifyNewOrder(orderId, username, customerName);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "New order notification sent"
        ));
    }

    /**
     * Test order status change notification
     * POST /api/notifications/test/order-status
     */
    @PostMapping("/test/order-status")
    public ResponseEntity<?> testOrderStatus(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.getOrDefault("orderId", 123).toString());
        String username = (String) request.getOrDefault("username", "user1");
        String oldStatus = (String) request.getOrDefault("oldStatus", "PENDING");
        String newStatus = (String) request.getOrDefault("newStatus", "CONFIRMED");

        notificationService.notifyOrderStatusChanged(orderId, username, oldStatus, newStatus);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Order status notification sent"
        ));
    }

    /**
     * Test payment notification
     * POST /api/notifications/test/payment
     */
    @PostMapping("/test/payment")
    public ResponseEntity<?> testPayment(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.getOrDefault("orderId", 123).toString());
        String username = (String) request.getOrDefault("username", "user1");
        Double amount = Double.valueOf(request.getOrDefault("amount", 150000).toString());

        notificationService.notifyPaymentReceived(orderId, username, amount);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Payment notification sent"
        ));
    }

    /**
     * Test new product notification
     * POST /api/notifications/test/new-product
     */
    @PostMapping("/test/new-product")
    public ResponseEntity<?> testNewProduct(@RequestBody Map<String, Object> request) {
        Long productId = Long.valueOf(request.getOrDefault("productId", 1).toString());
        String productName = (String) request.getOrDefault("productName", "Trà Sữa Trân Châu Đường Đen");

        notificationService.notifyNewProduct(productId, productName);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "New product notification sent"
        ));
    }

    // ==================== WEBSOCKET MESSAGE HANDLERS ====================

    /**
     * Handle messages sent to /app/send-notification
     * Broadcasts to /topic/notifications
     */
    @MessageMapping("/send-notification")
    @SendTo("/topic/notifications")
    public NotificationMessage sendNotification(@Payload NotificationMessage message, 
                                                 SimpMessageHeaderAccessor headerAccessor) {
        // Get session attributes
        String sessionId = headerAccessor.getSessionId();
        
        // Log incoming message
        System.out.println("Received notification from session: " + sessionId);
        System.out.println("Message: " + message.getMessage());

        // Set sender from session if available
        if (message.getSender() == null && headerAccessor.getSessionAttributes() != null) {
            Object username = headerAccessor.getSessionAttributes().get("username");
            if (username != null) {
                message.setSender(username.toString());
            }
        }

        return message;
    }

    /**
     * Handle chat messages sent to /app/chat
     * Sends to specific user
     */
    @MessageMapping("/chat")
    public void sendChatMessage(@Payload Map<String, String> chatMessage,
                                SimpMessageHeaderAccessor headerAccessor) {
        String targetUser = chatMessage.get("targetUser");
        String message = chatMessage.get("message");
        String sender = chatMessage.get("sender");

        if (targetUser != null && message != null) {
            notificationService.sendChatMessage(targetUser, sender, message);
        }
    }

    /**
     * Get notification types (for frontend)
     * GET /api/notifications/types
     */
    @GetMapping("/types")
    public ResponseEntity<?> getNotificationTypes() {
        NotificationType[] types = NotificationType.values();
        
        Map<String, Object> response = new HashMap<>();
        response.put("types", types);
        response.put("count", types.length);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check with WebSocket info
     * GET /api/notifications/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "WebSocket Notification Service");
        info.put("status", "running");
        info.put("endpoints", Map.of(
                "websocket", "ws://localhost:8080/ws",
                "rest", "http://localhost:8080/api/notifications"
        ));
        info.put("subscriptions", Map.of(
                "broadcast", "/topic/notifications",
                "user", "/user/{username}/queue/notifications",
                "admin", "/topic/admin/notifications"
        ));
        info.put("messageMappings", Map.of(
                "sendNotification", "/app/send-notification",
                "chat", "/app/chat"
        ));

        return ResponseEntity.ok(info);
    }
}