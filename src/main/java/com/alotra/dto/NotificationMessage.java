package com.alotra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NotificationMessage DTO - Format for WebSocket notification messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {

    /**
     * Notification types
     */
    public enum NotificationType {
        ORDER_CREATED,      // New order created
        ORDER_UPDATED,      // Order status changed
        ORDER_CANCELLED,    // Order cancelled
        PAYMENT_RECEIVED,   // Payment confirmed
        PRODUCT_CREATED,    // New product added
        PRODUCT_UPDATED,    // Product updated
        REVIEW_ADDED,       // New review posted
        USER_REGISTERED,    // New user registered
        SYSTEM_ALERT,       // System notification
        CHAT_MESSAGE,       // Chat message
        GENERAL             // General notification
    }

    /**
     * Message ID (unique)
     */
    private String id;

    /**
     * Notification type
     */
    private NotificationType type;

    /**
     * Title of notification
     */
    private String title;

    /**
     * Message content
     */
    private String message;

    /**
     * Target user (null = broadcast to all)
     */
    private String targetUser;

    /**
     * Sender username (optional)
     */
    private String sender;

    /**
     * Related entity ID (e.g., orderId, productId)
     */
    private Long entityId;

    /**
     * Timestamp
     */
    private LocalDateTime timestamp;

    /**
     * Additional data (JSON format)
     */
    private Object data;

    /**
     * Priority level (HIGH, MEDIUM, LOW)
     */
    private String priority;

    /**
     * Is this notification read?
     */
    private boolean read;

    /**
     * Action URL (optional - where to redirect when clicked)
     */
    private String actionUrl;
}