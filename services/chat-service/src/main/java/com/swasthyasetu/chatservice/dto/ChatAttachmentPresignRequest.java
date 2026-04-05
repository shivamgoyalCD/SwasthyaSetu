package com.swasthyasetu.chatservice.dto;

import lombok.Data;

@Data
public class ChatAttachmentPresignRequest {
  private String conversationId;
  private String fileName;
  private String contentType;
  private Long size;
}
