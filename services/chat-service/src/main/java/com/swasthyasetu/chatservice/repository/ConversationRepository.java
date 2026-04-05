package com.swasthyasetu.chatservice.repository;

import com.swasthyasetu.chatservice.domain.Conversation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
  Optional<Conversation> findByAppointmentId(UUID appointmentId);
}
