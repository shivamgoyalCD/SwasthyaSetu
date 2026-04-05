package com.swasthyasetu.rtcsignalingservice.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TranslationClient {
  private final RestClient restClient;

  public TranslationClient(@Value("${translation-service.base-url}") String translationServiceBaseUrl) {
    this.restClient = RestClient.builder()
        .baseUrl(translationServiceBaseUrl)
        .build();
  }

  public String translateText(String text, String sourceLang, String targetLang) {
    try {
      TranslateTextResponse response = restClient.post()
          .uri("/translate/text")
          .body(Map.of(
              "text", text,
              "sourceLang", sourceLang,
              "targetLang", targetLang
          ))
          .retrieve()
          .body(TranslateTextResponse.class);
      return response != null ? response.translatedText() : null;
    } catch (Exception ex) {
      return null;
    }
  }

  private record TranslateTextResponse(
      String translatedText,
      double confidence
  ) {
  }
}
