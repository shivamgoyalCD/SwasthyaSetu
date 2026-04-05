package com.swasthyasetu.chatservice.dto;

import lombok.Data;

@Data
public class SocketChatRequest {
  private String action;
  private String conversationId;
  private CreateChatMessageRequest payload;
}
