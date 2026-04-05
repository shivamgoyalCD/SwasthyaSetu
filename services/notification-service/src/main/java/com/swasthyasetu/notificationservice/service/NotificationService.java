package com.swasthyasetu.notificationservice.service;

import com.swasthyasetu.notificationservice.domain.Notification;
import com.swasthyasetu.notificationservice.dto.NotifyRequest;
import com.swasthyasetu.notificationservice.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
  private static final String QUEUED_STATUS = "QUEUED";

  private final NotificationRepository notificationRepository;

  public Notification queueNotification(UUID userId, NotifyRequest request) {
    Notification notification = new Notification(
        UUID.randomUUID(),
        userId,
        request.getType(),
        request.getPayload(),
        QUEUED_STATUS,
        0,
        LocalDateTime.now()
    );

    Notification saved = notificationRepository.save(notification);
    log.info(
        "Queued notification id={} userId={} type={} payload={}",
        saved.getId(),
        saved.getUserId(),
        saved.getType(),
        saved.getPayload()
    );
    return saved;
  }
}
