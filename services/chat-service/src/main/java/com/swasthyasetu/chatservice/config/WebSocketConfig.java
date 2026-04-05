package com.swasthyasetu.chatservice.config;

import com.swasthyasetu.chatservice.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
  private final ChatWebSocketHandler chatWebSocketHandler;
  private final WebSocketAuthHandshakeInterceptor webSocketAuthHandshakeInterceptor;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(chatWebSocketHandler, "/ws/chat")
        .addInterceptors(webSocketAuthHandshakeInterceptor)
        .setAllowedOriginPatterns("*");
  }
}
