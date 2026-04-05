package com.swasthyasetu.translationservice.dto;

public record TranslateTextResponse(
    String translatedText,
    double confidence
) {
}
