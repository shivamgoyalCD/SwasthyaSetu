package com.swasthyasetu.chatservice.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TranslationClient {
  private final RestClient restClient;

  public TranslationClient(
      @Value("${translation-service.base-url}") String translationServiceBaseUrl
  ) {
    this.restClient = RestClient.builder()
        .baseUrl(translationServiceBaseUrl)
        .build();
  }

  public String translateText(String content, String originalLang, String targetLang) {
    try {
      Object response = restClient.post()
          .uri("/translate/text")
          .body(Map.of(
              "text", content,
              "sourceLang", originalLang,
              "targetLang", targetLang
          ))
          .retrieve()
          .body(Object.class);
      return extractTranslatedContent(response);
    } catch (Exception ex) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private String extractTranslatedContent(Object response) {
    if (response == null) {
      return null;
    }
    if (response instanceof String value) {
      return value;
    }
    if (response instanceof Map<?, ?> map) {
      Object data = map.get("data");
      if (data != null) {
        String nested = extractTranslatedContent(data);
        if (nested != null && !nested.isBlank()) {
          return nested;
        }
      }

      for (String key : new String[]{"translatedContent", "translated_content", "translatedText", "translation", "text"}) {
        Object value = ((Map<String, Object>) map).get(key);
        if (value instanceof String translated && !translated.isBlank()) {
          return translated;
        }
      }
    }
    return null;
  }
}
