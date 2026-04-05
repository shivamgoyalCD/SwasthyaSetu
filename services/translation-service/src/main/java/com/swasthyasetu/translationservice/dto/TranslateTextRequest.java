package com.swasthyasetu.translationservice.dto;

public record TranslateTextRequest(
    String text,
    String sourceLang,
    String targetLang
) {
}
