package com.swasthyasetu.userservice.service;

import com.swasthyasetu.userservice.domain.User;
import com.swasthyasetu.userservice.dto.UpdateUserRequest;
import com.swasthyasetu.userservice.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public User getOrCreate(UUID userId, String role) {
    User user = userRepository.findById(userId).orElseGet(() ->
        new User(userId, null, role, null, null, LocalDateTime.now())
    );
    if (!role.equals(user.getRole())) {
      user.setRole(role);
    }
    return userRepository.save(user);
  }

  public User update(UUID userId, String role, UpdateUserRequest request) {
    User user = getOrCreate(userId, role);
    if (request.getName() != null) {
      user.setName(request.getName());
    }
    if (request.getLanguage() != null) {
      user.setLanguage(request.getLanguage());
    }
    return userRepository.save(user);
  }
}
