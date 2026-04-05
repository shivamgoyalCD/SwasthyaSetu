package com.swasthyasetu.chatservice.service;

import com.swasthyasetu.chatservice.domain.Conversation;
import com.swasthyasetu.chatservice.dto.ChatAttachmentPresignRequest;
import com.swasthyasetu.chatservice.dto.ChatAttachmentPresignResponse;
import com.swasthyasetu.chatservice.security.CurrentUser;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentService {
  private final ConversationService conversationService;
  private final MinioClient minioClient;

  @Value("${minio.bucket}")
  private String minioBucket;

  public Optional<ChatAttachmentPresignResponse> presignAttachment(
      UUID conversationId,
      CurrentUser currentUser,
      ChatAttachmentPresignRequest request
  ) throws Exception {
    Optional<Conversation> conversation = conversationService.validateAccess(conversationId, currentUser);
    if (conversation.isEmpty()) {
      return Optional.empty();
    }

    String safeFileName = sanitizeFileName(request.getFileName());
    String s3Key = "chat-attachments/" + conversationId + "/" + UUID.randomUUID() + "-" + safeFileName;
    String uploadUrl = minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(minioBucket)
            .object(s3Key)
            .expiry(15, TimeUnit.MINUTES)
            .build()
    );

    return Optional.of(new ChatAttachmentPresignResponse(uploadUrl, s3Key));
  }

  private String sanitizeFileName(String fileName) {
    return fileName.trim().replace("\\", "_").replace("/", "_");
  }
}
