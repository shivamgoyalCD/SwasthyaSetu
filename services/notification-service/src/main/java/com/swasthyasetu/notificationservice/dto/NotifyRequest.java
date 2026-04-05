package com.swasthyasetu.notificationservice.dto;

import lombok.Data;

@Data
public class NotifyRequest {
  private String userId;
  private String type;
  private String payload;
}
