package com.alotra.service;

import com.alotra.dto.NotificationMessage;
import com.alotra.dto.NotificationMessage.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * NotificationService - Service for sending real-time notifications via WebSocket
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to all connected clients (broadcast)
     * 
     * @param message Notification message
     */
    public void sendToAll(NotificationMessage message) {
        // Set timestamp if not set
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        
        // Set ID if not set
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }

        // Send to /topic/notifications - all subscribers will receive
        messagingTemplate.convertAndSend("/topic/notifications", message);
        
        logger.info("Broadcast notification sent: {} - {}", message.getType(), message.getTitle());
    }

    /**
     * Send notification to specific user
     * 
     * @param username Target username
     * @param message Notification message
     */
    public void sendToUser(String username, NotificationMessage message) {
        if (username == null || username.trim().isEmpty()) {
            logger.error("Cannot send notification to null/empty username");
            return;
        }

        // Set timestamp and ID
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }
        
        message.setTargetUser(username);

        // Send to /user/{username}/queue/notifications
        messagingTemplate.convertAndSendToUser(username, "/queue/notifications", message);
        
        logger.info("User notification sent to '{}': {} - {}", username, message.getType(), message.getTitle());
    }

    /**
     * Send notification to admin users only
     * 
     * @param message Notification message
     */
    public void sendToAdmins(NotificationMessage message) {
        // Set timestamp and ID
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        if (message.getId() == null) {
            message.setId(UUID.randomUUID().toString());
        }

        // Send to /topic/admin/notifications
        messagingTemplate.convertAndSend("/topic/admin/notifications", message);
        
        logger.info("Admin notification sent: {} - {}", message.getType(), message.getTitle());
    }

    // ==================== CONVENIENCE METHODS ====================

    /**
     * Notify about new order
     */
    public void notifyNewOrder(Long orderId, String username, String customerName) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.ORDER_CREATED)
                .title("Đơn hàng mới")
                .message(String.format("Khách hàng %s vừa đặt đơn hàng mới", customerName))
                .entityId(orderId)
                .priority("HIGH")
                .actionUrl("/admin/orders/" + orderId)
                .build();
        
        // Send to admins
        sendToAdmins(message);
        
        // Also send to the user who created the order
        NotificationMessage userMessage = NotificationMessage.builder()
                .type(NotificationType.ORDER_CREATED)
                .title("Đơn hàng đã được tạo")
                .message("Đơn hàng của bạn đã được tạo thành công và đang chờ xác nhận")
                .entityId(orderId)
                .priority("MEDIUM")
                .actionUrl("/orders/" + orderId)
                .build();
        
        sendToUser(username, userMessage);
    }

    /**
     * Notify about order status change
     */
    public void notifyOrderStatusChanged(Long orderId, String username, String oldStatus, String newStatus) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.ORDER_UPDATED)
                .title("Cập nhật đơn hàng")
                .message(String.format("Đơn hàng #%d đã chuyển từ '%s' sang '%s'", orderId, oldStatus, newStatus))
                .entityId(orderId)
                .priority("MEDIUM")
                .actionUrl("/orders/" + orderId)
                .build();
        
        sendToUser(username, message);
    }

    /**
     * Notify about payment confirmation
     */
    public void notifyPaymentReceived(Long orderId, String username, Double amount) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.PAYMENT_RECEIVED)
                .title("Thanh toán thành công")
                .message(String.format("Thanh toán %.0f VND cho đơn hàng #%d đã được xác nhận", amount, orderId))
                .entityId(orderId)
                .priority("HIGH")
                .actionUrl("/orders/" + orderId)
                .build();
        
        sendToUser(username, message);
    }

    /**
     * Notify about new product
     */
    public void notifyNewProduct(Long productId, String productName) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.PRODUCT_CREATED)
                .title("Sản phẩm mới")
                .message(String.format("Sản phẩm mới '%s' đã được thêm vào hệ thống", productName))
                .entityId(productId)
                .priority("LOW")
                .actionUrl("/products/" + productId)
                .build();
        
        sendToAll(message);
    }

    /**
     * Notify about new review
     */
    public void notifyNewReview(Long productId, String productName, String reviewerName, int rating) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.REVIEW_ADDED)
                .title("Đánh giá mới")
                .message(String.format("%s vừa đánh giá %d sao cho sản phẩm '%s'", 
                        reviewerName, rating, productName))
                .entityId(productId)
                .priority("LOW")
                .actionUrl("/products/" + productId)
                .build();
        
        sendToAdmins(message);
    }

    /**
     * Notify about new user registration
     */
    public void notifyNewUserRegistration(String username, String email) {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.USER_REGISTERED)
                .title("Người dùng mới")
                .message(String.format("Người dùng mới '%s' (%s) đã đăng ký", username, email))
                .priority("LOW")
                .build();
        
        sendToAdmins(message);
    }

    /**
     * Send system alert
     */
    public void sendSystemAlert(String title, String message, String priority) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.SYSTEM_ALERT)
                .title(title)
                .message(message)
                .priority(priority != null ? priority : "MEDIUM")
                .build();
        
        sendToAll(notification);
    }

    /**
     * Send chat message to specific user
     */
    public void sendChatMessage(String targetUser, String sender, String message) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.CHAT_MESSAGE)
                .title("Tin nhắn mới từ " + sender)
                .message(message)
                .sender(sender)
                .priority("MEDIUM")
                .build();
        
        sendToUser(targetUser, notification);
    }

    /**
     * Send custom notification to all
     */
    public void sendCustomNotification(String title, String message, NotificationType type) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .priority("MEDIUM")
                .build();
        
        sendToAll(notification);
    }

    /**
     * Send custom notification to specific user
     */
    public void sendCustomNotificationToUser(String username, String title, String message, NotificationType type) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(type)
                .title(title)
                .message(message)
                .priority("MEDIUM")
                .build();
        
        sendToUser(username, notification);
    }
}