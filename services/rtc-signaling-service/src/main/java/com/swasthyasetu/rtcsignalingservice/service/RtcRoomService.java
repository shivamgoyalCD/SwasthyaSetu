package com.swasthyasetu.rtcsignalingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swasthyasetu.rtcsignalingservice.dto.CreateRtcRoomResponse;
import com.swasthyasetu.rtcsignalingservice.dto.RtcRoomStatusResponse;
import java.util.Collection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RtcRoomService {
  private final StringRedisTemplate stringRedisTemplate;
  private final ObjectMapper objectMapper;

  public CreateRtcRoomResponse createRoom(UUID appointmentId) {
    saveRoomState(appointmentId, new RoomState(Instant.now().toString(), List.of()));
    return new CreateRtcRoomResponse(appointmentId);
  }

  public boolean roomExists(UUID appointmentId) {
    return Boolean.TRUE.equals(stringRedisTemplate.hasKey(roomKey(appointmentId)));
  }

  public Optional<RtcRoomStatusResponse> getRoomStatus(UUID appointmentId) {
    return loadRoomState(appointmentId)
        .map(roomState -> new RtcRoomStatusResponse(roomState.participants()));
  }

  public boolean syncParticipants(UUID appointmentId, Collection<UUID> participantIds) {
    Optional<RoomState> roomState = loadRoomState(appointmentId);
    if (roomState.isEmpty()) {
      return false;
    }

    List<String> participants = participantIds.stream()
        .map(UUID::toString)
        .distinct()
        .sorted()
        .toList();
    saveRoomState(appointmentId, new RoomState(roomState.get().createdAt(), participants));
    return true;
  }

  private String serializeRoomState(RoomState roomState) {
    try {
      return objectMapper.writeValueAsString(roomState);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to serialize RTC room state", ex);
    }
  }

  private Optional<RoomState> loadRoomState(UUID appointmentId) {
    String value = stringRedisTemplate.opsForValue().get(roomKey(appointmentId));
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }

    try {
      return Optional.of(objectMapper.readValue(value, RoomState.class));
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to deserialize RTC room state", ex);
    }
  }

  private void saveRoomState(UUID appointmentId, RoomState roomState) {
    stringRedisTemplate.opsForValue().set(roomKey(appointmentId), serializeRoomState(roomState));
  }

  private String roomKey(UUID appointmentId) {
    return "rtc:room:" + appointmentId;
  }

  private record RoomState(
      String createdAt,
      List<String> participants
  ) {
  }
}
