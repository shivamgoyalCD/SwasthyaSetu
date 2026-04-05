package com.swasthyasetu.rtcsignalingservice.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swasthyasetu.rtcsignalingservice.dto.SocketRtcRequest;
import com.swasthyasetu.rtcsignalingservice.service.RtcRoomService;
import com.swasthyasetu.rtcsignalingservice.service.TranslationClient;
import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class RtcWebSocketHandler extends TextWebSocketHandler {
  private final ObjectMapper objectMapper;
  private final RtcRoomService rtcRoomService;
  private final TranslationClient translationClient;

  private final Map<UUID, Map<UUID, Set<WebSocketSession>>> roomSessions = new ConcurrentHashMap<>();
  private final Map<String, Map<UUID, UUID>> sessionMemberships = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    cleanupSession(session);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    SocketRtcRequest request;
    try {
      request = objectMapper.readValue(message.getPayload(), SocketRtcRequest.class);
    } catch (JsonProcessingException ex) {
      sendError(session, "INVALID_REQUEST", "Malformed JSON payload");
      return;
    }

    if (request == null || isBlank(request.getAction())) {
      sendError(session, "INVALID_REQUEST", "action is required");
      return;
    }

    switch (request.getAction()) {
      case "joinRoom" -> handleJoinRoom(session, request);
      case "offer" -> handleOffer(session, request);
      case "answer" -> handleAnswer(session, request);
      case "iceCandidate" -> handleIceCandidate(session, request);
      case "caption" -> handleCaption(session, request);
      case "leaveRoom" -> handleLeaveRoom(session, request);
      default -> sendError(session, "INVALID_ACTION", "Unsupported action");
    }
  }

  private void handleJoinRoom(WebSocketSession session, SocketRtcRequest request) throws IOException {
    UUID appointmentId = parseUuid(request.getAppointmentId());
    if (appointmentId == null) {
      sendError(session, "INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID");
      return;
    }

    UUID userId = parseUuid(request.getUserId());
    if (userId == null) {
      sendError(session, "INVALID_USER_ID", "userId must be a valid UUID");
      return;
    }

    if (!rtcRoomService.roomExists(appointmentId)) {
      sendError(session, "ROOM_NOT_FOUND", "RTC room not found");
      return;
    }

    UUID previousUserId = sessionMemberships
        .computeIfAbsent(session.getId(), ignored -> new ConcurrentHashMap<>())
        .put(appointmentId, userId);

    if (previousUserId != null && !previousUserId.equals(userId)) {
      removeSessionFromRoom(appointmentId, previousUserId, session, false);
    }

    roomSessions.computeIfAbsent(appointmentId, ignored -> new ConcurrentHashMap<>())
        .computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
        .add(session);

    List<String> participants = currentParticipantIds(appointmentId);
    if (!rtcRoomService.syncParticipants(appointmentId, currentParticipants(appointmentId))) {
      removeMembership(session, appointmentId, userId, false);
      sendError(session, "ROOM_NOT_FOUND", "RTC room not found");
      return;
    }

    send(session, responsePayload(
        "joinedRoom",
        appointmentId,
        Map.of(
            "userId", userId.toString(),
            "participants", participants
        )
    ));
  }

  private void handleOffer(WebSocketSession session, SocketRtcRequest request) throws IOException {
    if (isBlank(request.getSdp())) {
      sendError(session, "INVALID_REQUEST", "sdp is required");
      return;
    }

    relaySdpMessage(session, request, "offer");
  }

  private void handleAnswer(WebSocketSession session, SocketRtcRequest request) throws IOException {
    if (isBlank(request.getSdp())) {
      sendError(session, "INVALID_REQUEST", "sdp is required");
      return;
    }

    relaySdpMessage(session, request, "answer");
  }

  private void handleIceCandidate(WebSocketSession session, SocketRtcRequest request) throws IOException {
    UUID appointmentId = parseUuid(request.getAppointmentId());
    if (appointmentId == null) {
      sendError(session, "INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID");
      return;
    }

    UUID toUserId = parseUuid(request.getToUserId());
    if (toUserId == null) {
      sendError(session, "INVALID_TARGET_USER_ID", "toUserId must be a valid UUID");
      return;
    }

    if (request.getCandidate() == null || request.getCandidate().isNull()) {
      sendError(session, "INVALID_REQUEST", "candidate is required");
      return;
    }

    UUID fromUserId = joinedUserId(session, appointmentId);
    if (fromUserId == null) {
      sendError(session, "ROOM_NOT_JOINED", "Join the RTC room before sending signaling messages");
      return;
    }

    Map<String, Object> payload = responsePayload(
        "iceCandidate",
        appointmentId,
        Map.of(
            "fromUserId", fromUserId.toString(),
            "candidate", request.getCandidate()
        )
    );
    if (!forwardToUser(appointmentId, toUserId, payload)) {
      sendError(session, "TARGET_NOT_CONNECTED", "Target user is not connected to this room");
    }
  }

  private void handleLeaveRoom(WebSocketSession session, SocketRtcRequest request) throws IOException {
    UUID appointmentId = parseUuid(request.getAppointmentId());
    if (appointmentId == null) {
      sendError(session, "INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID");
      return;
    }

    UUID userId = parseUuid(request.getUserId());
    if (userId == null) {
      sendError(session, "INVALID_USER_ID", "userId must be a valid UUID");
      return;
    }

    UUID joinedUserId = joinedUserId(session, appointmentId);
    if (joinedUserId == null) {
      sendError(session, "ROOM_NOT_JOINED", "Join the RTC room before leaving it");
      return;
    }
    if (!joinedUserId.equals(userId)) {
      sendError(session, "USER_ID_MISMATCH", "userId does not match the joined room membership");
      return;
    }

    removeMembership(session, appointmentId, userId, true);
    send(session, responsePayload(
        "leftRoom",
        appointmentId,
        Map.of(
            "userId", userId.toString(),
            "participants", currentParticipantIds(appointmentId)
        )
    ));
  }

  private void handleCaption(WebSocketSession session, SocketRtcRequest request) throws IOException {
    UUID appointmentId = parseUuid(request.getAppointmentId());
    if (appointmentId == null) {
      sendError(session, "INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID");
      return;
    }
    if (joinedUserId(session, appointmentId) == null) {
      sendError(session, "ROOM_NOT_JOINED", "Join the RTC room before sending captions");
      return;
    }
    if (isBlank(request.getSpeaker())
        || isBlank(request.getText())
        || isBlank(request.getLang())
        || isBlank(request.getTargetLang())) {
      sendError(session, "INVALID_REQUEST", "speaker, text, lang and targetLang are required");
      return;
    }

    String translatedText = translationClient.translateText(
        request.getText(),
        request.getLang(),
        request.getTargetLang()
    );
    if (isBlank(translatedText)) {
      sendError(session, "TRANSLATION_FAILED", "Failed to translate caption text");
      return;
    }

    broadcast(
        appointmentId,
        responsePayload(
            "caption",
            appointmentId,
            Map.of(
                "speaker", request.getSpeaker(),
                "originalText", request.getText(),
                "translatedText", translatedText,
                "ts", Instant.now().toString()
            )
        )
    );
  }

  private void relaySdpMessage(WebSocketSession session, SocketRtcRequest request, String action) throws IOException {
    UUID appointmentId = parseUuid(request.getAppointmentId());
    if (appointmentId == null) {
      sendError(session, "INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID");
      return;
    }

    UUID toUserId = parseUuid(request.getToUserId());
    if (toUserId == null) {
      sendError(session, "INVALID_TARGET_USER_ID", "toUserId must be a valid UUID");
      return;
    }

    UUID fromUserId = joinedUserId(session, appointmentId);
    if (fromUserId == null) {
      sendError(session, "ROOM_NOT_JOINED", "Join the RTC room before sending signaling messages");
      return;
    }

    Map<String, Object> payload = responsePayload(
        action,
        appointmentId,
        Map.of(
            "fromUserId", fromUserId.toString(),
            "sdp", request.getSdp()
        )
    );
    if (!forwardToUser(appointmentId, toUserId, payload)) {
      sendError(session, "TARGET_NOT_CONNECTED", "Target user is not connected to this room");
    }
  }

  private void broadcast(UUID appointmentId, Map<String, Object> payload) throws IOException {
    Map<UUID, Set<WebSocketSession>> room = roomSessions.get(appointmentId);
    if (room == null || room.isEmpty()) {
      return;
    }

    for (Set<WebSocketSession> userSessions : room.values()) {
      for (WebSocketSession targetSession : userSessions) {
        if (!targetSession.isOpen()) {
          continue;
        }
        send(targetSession, payload);
      }
    }
  }

  private boolean forwardToUser(UUID appointmentId, UUID toUserId, Map<String, Object> payload) throws IOException {
    Map<UUID, Set<WebSocketSession>> room = roomSessions.get(appointmentId);
    if (room == null || room.isEmpty()) {
      return false;
    }

    Set<WebSocketSession> targetSessions = room.get(toUserId);
    if (targetSessions == null || targetSessions.isEmpty()) {
      return false;
    }

    boolean delivered = false;
    for (WebSocketSession targetSession : targetSessions) {
      if (!targetSession.isOpen()) {
        continue;
      }
      send(targetSession, payload);
      delivered = true;
    }
    return delivered;
  }

  private void cleanupSession(WebSocketSession session) {
    Map<UUID, UUID> memberships = sessionMemberships.remove(session.getId());
    if (memberships == null || memberships.isEmpty()) {
      return;
    }

    for (Map.Entry<UUID, UUID> membership : memberships.entrySet()) {
      removeSessionFromRoom(membership.getKey(), membership.getValue(), session, true);
    }
  }

  private void removeMembership(WebSocketSession session, UUID appointmentId, UUID userId, boolean syncParticipants) {
    Map<UUID, UUID> memberships = sessionMemberships.get(session.getId());
    if (memberships != null) {
      UUID currentUserId = memberships.get(appointmentId);
      if (userId.equals(currentUserId)) {
        memberships.remove(appointmentId);
        if (memberships.isEmpty()) {
          sessionMemberships.remove(session.getId());
        }
      }
    }

    removeSessionFromRoom(appointmentId, userId, session, syncParticipants);
  }

  private void removeSessionFromRoom(
      UUID appointmentId,
      UUID userId,
      WebSocketSession session,
      boolean syncParticipants
  ) {
    Map<UUID, Set<WebSocketSession>> room = roomSessions.get(appointmentId);
    if (room == null) {
      return;
    }

    Set<WebSocketSession> sessions = room.get(userId);
    if (sessions != null) {
      sessions.remove(session);
      if (sessions.isEmpty()) {
        room.remove(userId);
      }
    }
    if (room.isEmpty()) {
      roomSessions.remove(appointmentId);
    }

    if (syncParticipants) {
      rtcRoomService.syncParticipants(appointmentId, currentParticipants(appointmentId));
    }
  }

  private UUID joinedUserId(WebSocketSession session, UUID appointmentId) {
    Map<UUID, UUID> memberships = sessionMemberships.get(session.getId());
    if (memberships == null) {
      return null;
    }
    return memberships.get(appointmentId);
  }

  private Set<UUID> currentParticipants(UUID appointmentId) {
    Map<UUID, Set<WebSocketSession>> room = roomSessions.get(appointmentId);
    if (room == null || room.isEmpty()) {
      return Set.of();
    }

    return room.entrySet().stream()
        .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
        .map(Map.Entry::getKey)
        .collect(java.util.stream.Collectors.toSet());
  }

  private List<String> currentParticipantIds(UUID appointmentId) {
    return currentParticipants(appointmentId).stream()
        .map(UUID::toString)
        .sorted()
        .toList();
  }

  private Map<String, Object> responsePayload(
      String action,
      UUID appointmentId,
      Map<String, Object> attributes
  ) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("action", action);
    payload.put("appointmentId", appointmentId.toString());
    payload.putAll(attributes);
    return payload;
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

  private UUID parseUuid(String value) {
    if (isBlank(value)) {
      return null;
    }

    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
