package com.swasthyasetu.translationservice.api;

import com.swasthyasetu.translationservice.dto.TranslateTextRequest;
import com.swasthyasetu.translationservice.dto.TranslateTextResponse;
import com.swasthyasetu.translationservice.service.TranslationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translate")
public class TranslationController {
  private final TranslationService translationService;

  public TranslationController(TranslationService translationService) {
    this.translationService = translationService;
  }

  @PostMapping("/text")
  public TranslateTextResponse translateText(@RequestBody TranslateTextRequest request) {
    return translationService.translateText(request);
  }
}
