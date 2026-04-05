package com.swasthyasetu.chatservice.service;

import com.swasthyasetu.chatservice.domain.ChatMessage;
import com.swasthyasetu.chatservice.domain.Conversation;
import com.swasthyasetu.chatservice.dto.CreateChatMessageRequest;
import com.swasthyasetu.chatservice.repository.ChatMessageRepository;
import com.swasthyasetu.chatservice.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
  private static final int PAGE_SIZE = 30;
  private static final String TEXT_TYPE = "TEXT";

  private final ConversationService conversationService;
  private final ConversationRepository conversationRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final TranslationClient translationClient;

  @Transactional
  public Optional<ChatMessage> createMessage(
      UUID conversationId,
      CurrentUser currentUser,
      CreateChatMessageRequest request
  ) {
    Optional<Conversation> conversation = conversationService.claimOrValidateAccess(conversationId, currentUser);
    if (conversation.isEmpty()) {
      return Optional.empty();
    }

    Conversation existingConversation = conversation.get();
    ChatMessage message = new ChatMessage(
        UUID.randomUUID(),
        existingConversation.getId(),
        currentUser.getId(),
        request.getType(),
        request.getContent(),
        request.getOriginalLang(),
        null,
        LocalDateTime.now()
    );
    ChatMessage saved = chatMessageRepository.save(message);

    String translatedContent = translationClient.translateText(
        request.getContent(),
        request.getOriginalLang(),
        request.getTargetLang()
    );
    if (translatedContent != null) {
      saved.setTranslatedContent(translatedContent);
      saved = chatMessageRepository.save(saved);
    }

    return Optional.of(saved);
  }

  @Transactional(readOnly = true)
  public Optional<MessagePage> getMessages(
      UUID conversationId,
      CurrentUser currentUser,
      LocalDateTime cursor
  ) {
    Optional<Conversation> conversation = conversationService.validateAccess(conversationId, currentUser);
    if (conversation.isEmpty()) {
      return Optional.empty();
    }

    List<ChatMessage> fetched = cursor == null
        ? chatMessageRepository.findByConversationIdOrderByCreatedAtDesc(
            conversationId,
            PageRequest.of(0, PAGE_SIZE + 1)
        )
        : chatMessageRepository.findByConversationIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            conversationId,
            cursor,
            PageRequest.of(0, PAGE_SIZE + 1)
        );

    boolean hasMore = fetched.size() > PAGE_SIZE;
    if (hasMore) {
      fetched = new ArrayList<>(fetched.subList(0, PAGE_SIZE));
    } else {
      fetched = new ArrayList<>(fetched);
    }

    String nextCursor = hasMore && !fetched.isEmpty()
        ? fetched.get(fetched.size() - 1).getCreatedAt().toString()
        : null;

    Collections.reverse(fetched);
    return Optional.of(new MessagePage(fetched, nextCursor));
  }

  @Transactional(readOnly = true)
  public List<ChatMessage> getAppointmentTextMessages(UUID appointmentId) {
    return conversationRepository.findByAppointmentId(appointmentId)
        .map(conversation -> chatMessageRepository.findByConversationIdAndTypeOrderByCreatedAtAsc(
            conversation.getId(),
            TEXT_TYPE
        ))
        .orElseGet(List::of);
  }

  public record MessagePage(List<ChatMessage> items, String nextCursor) {
  }
}
