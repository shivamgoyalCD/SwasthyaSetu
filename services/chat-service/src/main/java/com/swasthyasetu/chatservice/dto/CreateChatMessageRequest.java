package com.swasthyasetu.chatservice.dto;

import lombok.Data;

@Data
public class CreateChatMessageRequest {
  private String type;
  private String content;
  private String originalLang;
  private String targetLang;
}
