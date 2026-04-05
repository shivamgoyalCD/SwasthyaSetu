package com.swasthyasetu.chatservice.service;

import com.swasthyasetu.chatservice.domain.Conversation;
import com.swasthyasetu.chatservice.repository.ConversationRepository;
import com.swasthyasetu.chatservice.security.CurrentUser;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversationService {
  private static final String ACTIVE_STATUS = "ACTIVE";

  private final ConversationRepository conversationRepository;

  @Transactional
  public Conversation startConversation(UUID appointmentId, CurrentUser currentUser) {
    Optional<Conversation> existing = conversationRepository.findByAppointmentId(appointmentId);
    if (existing.isPresent()) {
      return updateParticipantsIfMissing(existing.get(), currentUser);
    }

    Conversation conversation = new Conversation(
        UUID.randomUUID(),
        appointmentId,
        resolvePatientId(currentUser),
        resolveDoctorId(currentUser),
        ACTIVE_STATUS,
        LocalDateTime.now()
    );

    try {
      return conversationRepository.saveAndFlush(conversation);
    } catch (DataIntegrityViolationException ex) {
      return conversationRepository.findByAppointmentId(appointmentId)
          .map(found -> updateParticipantsIfMissing(found, currentUser))
          .orElseThrow(() -> ex);
    }
  }

  @Transactional
  public Optional<Conversation> claimOrValidateAccess(UUID conversationId, CurrentUser currentUser) {
    return conversationRepository.findById(conversationId)
        .map(conversation -> updateParticipantsIfMissing(conversation, currentUser));
  }

  @Transactional(readOnly = true)
  public Optional<Conversation> validateAccess(UUID conversationId, CurrentUser currentUser) {
    return conversationRepository.findById(conversationId)
        .map(conversation -> validateConversationAccess(conversation, currentUser));
  }

  private Conversation updateParticipantsIfMissing(Conversation conversation, CurrentUser currentUser) {
    boolean changed = false;

    if ("PATIENT".equals(currentUser.getRole()) && conversation.getPatientId() == null) {
      conversation.setPatientId(currentUser.getId());
      changed = true;
    }
    if ("DOCTOR".equals(currentUser.getRole()) && conversation.getDoctorId() == null) {
      conversation.setDoctorId(currentUser.getId());
      changed = true;
    }
    validateConversationAccess(conversation, currentUser);
    if (conversation.getStatus() == null || conversation.getStatus().isBlank()) {
      conversation.setStatus(ACTIVE_STATUS);
      changed = true;
    }

    return changed ? conversationRepository.save(conversation) : conversation;
  }

  private Conversation validateConversationAccess(Conversation conversation, CurrentUser currentUser) {
    if ("PATIENT".equals(currentUser.getRole())
        && conversation.getPatientId() != null
        && !currentUser.getId().equals(conversation.getPatientId())) {
      throw new ConversationAccessForbiddenException("Patient does not belong to this conversation");
    }

    if ("DOCTOR".equals(currentUser.getRole())
        && conversation.getDoctorId() != null
        && !currentUser.getId().equals(conversation.getDoctorId())) {
      throw new ConversationAccessForbiddenException("Doctor does not belong to this conversation");
    }

    return conversation;
  }

  private UUID resolvePatientId(CurrentUser currentUser) {
    return "PATIENT".equals(currentUser.getRole()) ? currentUser.getId() : null;
  }

  private UUID resolveDoctorId(CurrentUser currentUser) {
    return "DOCTOR".equals(currentUser.getRole()) ? currentUser.getId() : null;
  }
}
