package com.swasthyasetu.chatservice.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConversationStartResponse {
  private UUID conversationId;
}
