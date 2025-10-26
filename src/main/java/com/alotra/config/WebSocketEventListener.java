package com.alotra.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * WebSocketEventListener - Listen to WebSocket connection events
 * Logs connections, disconnections, subscriptions for monitoring
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    /**
     * Handle WebSocket connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket Connected! Session ID: {}", sessionId);
        
        // You can track active connections here
        // For example: activeConnections.add(sessionId);
    }

    /**
     * Handle WebSocket disconnection event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("WebSocket Disconnected! Session ID: {}", sessionId);
        
        // Clean up resources for disconnected session
        // For example: activeConnections.remove(sessionId);
        
        // Get user from session attributes if needed
        Object username = headerAccessor.getSessionAttributes() != null 
                ? headerAccessor.getSessionAttributes().get("username") 
                : null;
        
        if (username != null) {
            logger.info("User '{}' disconnected", username);
        }
    }

    /**
     * Handle subscription event
     */
    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        
        logger.info("New Subscription - Session: {}, Destination: {}", sessionId, destination);
    }

    /**
     * Handle unsubscription event
     */
    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("Unsubscribed - Session: {}", sessionId);
    }
}