package com.swasthyasetu.chatservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "conversation_id", nullable = false, columnDefinition = "uuid")
  private UUID conversationId;

  @Column(name = "sender_id", nullable = false, columnDefinition = "uuid")
  private UUID senderId;

  @Column(name = "type")
  private String type;

  @Column(name = "content")
  private String content;

  @Column(name = "original_lang")
  private String originalLang;

  @Column(name = "translated_content")
  private String translatedContent;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
