package com.swasthyasetu.chatservice.api;

import com.swasthyasetu.chatservice.dto.ChatAttachmentPresignRequest;
import com.swasthyasetu.chatservice.dto.ChatAttachmentPresignResponse;
import com.swasthyasetu.chatservice.security.CurrentUser;
import com.swasthyasetu.chatservice.security.CurrentUserService;
import com.swasthyasetu.chatservice.service.AttachmentService;
import com.swasthyasetu.chatservice.service.ConversationAccessForbiddenException;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AttachmentController {
  private final AttachmentService attachmentService;
  private final CurrentUserService currentUserService;

  @PostMapping("/chat/attachments/presign")
  public ResponseEntity<ApiResponse<ChatAttachmentPresignResponse>> presign(
      @RequestBody ChatAttachmentPresignRequest request
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
        || isBlank(request.getConversationId())
        || isBlank(request.getFileName())
        || isBlank(request.getContentType())
        || request.getSize() == null
        || request.getSize() <= 0) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError(
              "INVALID_REQUEST",
              "conversationId, fileName, contentType and positive size are required",
              null
          )
      ));
    }

    UUID conversationId;
    try {
      conversationId = UUID.fromString(request.getConversationId());
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_CONVERSATION_ID", "conversationId must be a valid UUID", null)
      ));
    }

    try {
      return attachmentService.presignAttachment(conversationId, currentUser.get(), request)
          .map(response -> ResponseEntity.ok(new ApiResponse<>(true, response, null)))
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
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
          false,
          null,
          new ApiError("PRESIGN_FAILED", "Failed to generate upload URL", null)
      ));
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
