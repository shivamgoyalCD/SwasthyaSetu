package com.swasthyasetu.chatservice.api;

import com.swasthyasetu.chatservice.domain.ChatMessage;
import com.swasthyasetu.chatservice.dto.CreateChatMessageRequest;
import com.swasthyasetu.chatservice.security.CurrentUser;
import com.swasthyasetu.chatservice.security.CurrentUserService;
import com.swasthyasetu.chatservice.service.ChatMessageService;
import com.swasthyasetu.chatservice.service.ConversationAccessForbiddenException;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import com.swasthyasetu.common.dtos.PageResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {
  private final ChatMessageService chatMessageService;
  private final CurrentUserService currentUserService;

  @GetMapping("/chat/conversations/{id}/messages")
  public ResponseEntity<ApiResponse<PageResponse<ChatMessage>>> getMessages(
      @PathVariable("id") String id,
      @RequestParam(value = "cursor", required = false) String cursor
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"PATIENT".equals(currentUser.get().getRole()) && !"DOCTOR".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Patient or doctor role required", null)
      ));
    }

    UUID conversationId;
    try {
      conversationId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_CONVERSATION_ID", "id must be a valid UUID", null)
      ));
    }

    LocalDateTime parsedCursor = null;
    if (!isBlank(cursor)) {
      try {
        parsedCursor = LocalDateTime.parse(cursor);
      } catch (DateTimeParseException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(
            false,
            null,
            new ApiError("INVALID_CURSOR", "cursor must be a valid ISO local datetime", null)
        ));
      }
    }

    try {
      return chatMessageService.getMessages(conversationId, currentUser.get(), parsedCursor)
          .map(page -> ResponseEntity.ok(new ApiResponse<>(
              true,
              new PageResponse<>(page.items(), page.nextCursor()),
              null
          )))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("CONVERSATION_NOT_FOUND", "Conversation not found", null)
          )));
    } catch (ConversationAccessForbiddenException ex) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Caller does not belong to this conversation", null)
      ));
    }
  }

  @GetMapping("/chat/appointments/{appointmentId}/messages")
  public ResponseEntity<ApiResponse<java.util.List<ChatMessage>>> getAppointmentMessages(
      @PathVariable("appointmentId") String appointmentId
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"DOCTOR".equals(currentUser.get().getRole()) && !"ADMIN".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor or admin role required", null)
      ));
    }

    UUID parsedAppointmentId;
    try {
      parsedAppointmentId = UUID.fromString(appointmentId);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID", null)
      ));
    }

    return ResponseEntity.ok(new ApiResponse<>(
        true,
        chatMessageService.getAppointmentTextMessages(parsedAppointmentId),
        null
    ));
  }

  @PostMapping("/chat/conversations/{id}/messages")
  public ResponseEntity<ApiResponse<ChatMessage>> createMessage(
      @PathVariable("id") String id,
      @RequestBody CreateChatMessageRequest request
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"PATIENT".equals(currentUser.get().getRole()) && !"DOCTOR".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Patient or doctor role required", null)
      ));
    }

    if (request == null
        || !isTextType(request.getType())
        || isBlank(request.getContent())
        || isBlank(request.getOriginalLang())
        || isBlank(request.getTargetLang())) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError(
              "INVALID_REQUEST",
              "type=TEXT, content, originalLang and targetLang are required",
              null
          )
      ));
    }

    UUID conversationId;
    try {
      conversationId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_CONVERSATION_ID", "id must be a valid UUID", null)
      ));
    }

    try {
      return chatMessageService.createMessage(conversationId, currentUser.get(), request)
          .map(message -> ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, message, null)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("CONVERSATION_NOT_FOUND", "Conversation not found", null)
          )));
    } catch (ConversationAccessForbiddenException ex) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Caller does not belong to this conversation", null)
      ));
    }
  }

  private boolean isTextType(String type) {
    return "TEXT".equals(type);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
