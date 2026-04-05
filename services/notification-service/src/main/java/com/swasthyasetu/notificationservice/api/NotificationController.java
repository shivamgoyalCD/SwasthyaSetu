package com.swasthyasetu.notificationservice.api;

import com.swasthyasetu.notificationservice.dto.NotifyRequest;
import com.swasthyasetu.notificationservice.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NotificationController {
  private final NotificationService notificationService;

  @PostMapping("/notifications/notify")
  public ResponseEntity<String> notify(@RequestBody NotifyRequest request) {
    if (request == null || isBlank(request.getUserId()) || isBlank(request.getType()) || isBlank(request.getPayload())) {
      return ResponseEntity.badRequest().body("userId, type and payload are required");
    }

    UUID userId;
    try {
      userId = UUID.fromString(request.getUserId());
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body("userId must be a valid UUID");
    }

    notificationService.queueNotification(userId, request);
    return ResponseEntity.status(HttpStatus.OK).body("OK");
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
