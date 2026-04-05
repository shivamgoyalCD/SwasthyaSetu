package com.swasthyasetu.translationservice.service;

import com.swasthyasetu.translationservice.dto.TranslateTextRequest;
import com.swasthyasetu.translationservice.dto.TranslateTextResponse;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {
  public TranslateTextResponse translateText(TranslateTextRequest request) {
    return new TranslateTextResponse("[" + request.targetLang() + "] " + request.text(), 0.5);
  }
}
