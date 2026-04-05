package com.swasthyasetu.chatservice.repository;

import com.swasthyasetu.chatservice.domain.ChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
  List<ChatMessage> findByConversationIdAndTypeOrderByCreatedAtAsc(UUID conversationId, String type);

  List<ChatMessage> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

  List<ChatMessage> findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
      UUID conversationId,
      LocalDateTime createdAt,
      Pageable pageable
  );
}
