package com.swasthyasetu.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatAttachmentPresignResponse {
  private String uploadUrl;
  private String s3Key;
}
