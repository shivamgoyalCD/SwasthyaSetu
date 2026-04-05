package com.swasthyasetu.prescriptionservice.service;

import com.swasthyasetu.common.dtos.ApiResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ChatServiceClient {
  private static final ParameterizedTypeReference<ApiResponse<List<ChatMessagePayload>>> RESPONSE_TYPE =
      new ParameterizedTypeReference<>() {
      };

  private final RestClient restClient;

  public ChatServiceClient(@Value("${chat-service.base-url}") String chatServiceBaseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(chatServiceBaseUrl)
        .build();
  }

  public List<ChatMessagePayload> getAppointmentMessages(UUID appointmentId, String authorizationHeader) {
    try {
      ApiResponse<List<ChatMessagePayload>> response = restClient.get()
          .uri("/chat/appointments/{appointmentId}/messages", appointmentId)
          .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
          .retrieve()
          .body(RESPONSE_TYPE);

      if (response == null) {
        throw new ChatServiceException("chat-service returned an empty response");
      }
      if (!response.isSuccess()) {
        throw new ChatServiceException("chat-service returned an unsuccessful response");
      }
      return response.getData() == null ? List.of() : response.getData();
    } catch (ChatServiceException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ChatServiceException("Failed to fetch appointment messages from chat-service", ex);
    }
  }

  public record ChatMessagePayload(
      UUID id,
      UUID conversationId,
      UUID senderId,
      String type,
      String content,
      String originalLang,
      String translatedContent,
      LocalDateTime createdAt
  ) {
  }
}
