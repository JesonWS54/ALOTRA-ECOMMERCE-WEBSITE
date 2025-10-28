package AloTra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt WebSocket message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cấu hình tiền tố cho các đích đến mà client sẽ subscribe (lắng nghe)
        // Ví dụ: /topic/orders/1 (shop ID 1), /user/queue/private (tin nhắn riêng)
        config.enableSimpleBroker("/topic", "/queue");

        // Cấu hình tiền tố cho các đích đến mà client sẽ gửi message đến server
        // Ví dụ: /app/sendOrderNotification
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint mà client sẽ kết nối đến WebSocket server
        // "/ws" là endpoint phổ biến
        // withSockJS() cung cấp fallback cho các trình duyệt không hỗ trợ WebSocket thuần
        registry.addEndpoint("/ws").withSockJS();
    }
}