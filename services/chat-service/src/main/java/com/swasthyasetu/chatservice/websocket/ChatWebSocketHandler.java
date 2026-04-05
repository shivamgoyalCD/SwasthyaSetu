package com.swasthyasetu.chatservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swasthyasetu.chatservice.domain.ChatMessage;
import com.swasthyasetu.chatservice.dto.SocketChatRequest;
import com.swasthyasetu.chatservice.security.CurrentUser;
import com.swasthyasetu.chatservice.service.ChatMessageService;
import com.swasthyasetu.chatservice.service.ConversationAccessForbiddenException;
import com.swasthyasetu.chatservice.service.ConversationService;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
  private final ObjectMapper objectMapper;
  private final ConversationService conversationService;
  private final ChatMessageService chatMessageService;

  private final Map<UUID, Set<WebSocketSession>> conversationSessions = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    conversationSessions.values().forEach(sessions -> sessions.remove(session));
    conversationSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    SocketChatRequest request = objectMapper.readValue(message.getPayload(), SocketChatRequest.class);
    if (request == null || request.getAction() == null) {
      sendError(session, "INVALID_REQUEST", "action is required");
      return;
    }

    switch (request.getAction()) {
      case "join" -> handleJoin(session, request);
      case "message" -> handleMessage(session, request);
      default -> sendError(session, "INVALID_ACTION", "Unsupported action");
    }
  }

  private void handleJoin(WebSocketSession session, SocketChatRequest request) throws IOException {
    UUID conversationId = parseConversationId(request.getConversationId());
    if (conversationId == null) {
      sendError(session, "INVALID_CONVERSATION_ID", "conversationId must be a valid UUID");
      return;
    }

    try {
      Optional<?> conversation = conversationService.claimOrValidateAccess(conversationId, currentUser(session));
      if (conversation.isEmpty()) {
        sendError(session, "CONVERSATION_NOT_FOUND", "Conversation not found");
        return;
      }
      conversationSessions.computeIfAbsent(conversationId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
      send(session, Map.of(
          "action", "joined",
          "conversationId", conversationId.toString()
      ));
    } catch (ConversationAccessForbiddenException ex) {
      sendError(session, "FORBIDDEN", "Caller does not belong to this conversation");
    }
  }

  private void handleMessage(WebSocketSession session, SocketChatRequest request) throws IOException {
    UUID conversationId = parseConversationId(request.getConversationId());
    if (conversationId == null) {
      sendError(session, "INVALID_CONVERSATION_ID", "conversationId must be a valid UUID");
      return;
    }

    if (request.getPayload() == null) {
      sendError(session, "INVALID_REQUEST", "payload is required");
      return;
    }
    if (!"TEXT".equals(request.getPayload().getType())
        || isBlank(request.getPayload().getContent())
        || isBlank(request.getPayload().getOriginalLang())
        || isBlank(request.getPayload().getTargetLang())) {
      sendError(session, "INVALID_REQUEST", "type=TEXT, content, originalLang and targetLang are required");
      return;
    }

    try {
      Optional<ChatMessage> saved = chatMessageService.createMessage(conversationId, currentUser(session), request.getPayload());
      if (saved.isEmpty()) {
        sendError(session, "CONVERSATION_NOT_FOUND", "Conversation not found");
        return;
      }
      broadcast(conversationId, Map.of(
          "action", "message",
          "conversationId", conversationId.toString(),
          "payload", saved.get()
      ));
    } catch (ConversationAccessForbiddenException ex) {
      sendError(session, "FORBIDDEN", "Caller does not belong to this conversation");
    }
  }

  private void broadcast(UUID conversationId, Map<String, Object> payload) throws IOException {
    Set<WebSocketSession> sessions = conversationSessions.get(conversationId);
    if (sessions == null || sessions.isEmpty()) {
      return;
    }

    for (WebSocketSession session : sessions) {
      if (session.isOpen()) {
        send(session, payload);
      }
    }
  }

  private void sendError(WebSocketSession session, String code, String message) throws IOException {
    send(session, Map.of(
        "action", "error",
        "code", code,
        "message", message
    ));
  }

  private void send(WebSocketSession session, Map<String, Object> payload) throws IOException {
    synchronized (session) {
      session.sendMessage(new TextMessage(writeJson(payload)));
    }
  }

  private String writeJson(Map<String, Object> payload) throws JsonProcessingException {
    return objectMapper.writeValueAsString(payload);
  }

  private UUID parseConversationId(String conversationId) {
    if (isBlank(conversationId)) {
      return null;
    }
    try {
      return UUID.fromString(conversationId);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private CurrentUser currentUser(WebSocketSession session) {
    Object userId = session.getAttributes().get("userId");
    Object role = session.getAttributes().get("role");
    return new CurrentUser(UUID.fromString(String.valueOf(userId)), String.valueOf(role));
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
