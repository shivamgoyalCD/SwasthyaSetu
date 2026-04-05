package com.swasthyasetu.rtcsignalingservice.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SocketRtcRequest {
  private String action;
  private String appointmentId;
  private String userId;
  private String toUserId;
  private String sdp;
  private JsonNode candidate;
  private String speaker;
  private String text;
  private String lang;
  private String targetLang;
}
