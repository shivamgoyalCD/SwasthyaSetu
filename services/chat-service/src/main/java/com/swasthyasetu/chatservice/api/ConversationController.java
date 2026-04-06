package com.swasthyasetu.chatservice.api;

import com.swasthyasetu.chatservice.domain.Conversation;
import com.swasthyasetu.chatservice.dto.ConversationStartResponse;
import com.swasthyasetu.chatservice.security.CurrentUser;
import com.swasthyasetu.chatservice.security.CurrentUserService;
import com.swasthyasetu.chatservice.service.ConversationService;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConversationController {
  private final ConversationService conversationService;
  private final CurrentUserService currentUserService;

  @PostMapping("/chat/conversations/start")
  public ResponseEntity<ApiResponse<ConversationStartResponse>> startConversation(
      @RequestParam("appointmentId") String appointmentId
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

    Conversation conversation = conversationService.startConversation(parsedAppointmentId, currentUser.get());
    return ResponseEntity.ok(new ApiResponse<>(
        true,
        new ConversationStartResponse(conversation.getId()),
        null
    ));
  }
}
