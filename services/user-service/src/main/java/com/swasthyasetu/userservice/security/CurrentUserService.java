package com.swasthyasetu.userservice.security;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserService {
  public Optional<CurrentUser> getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      return Optional.empty();
    }

    String principal = String.valueOf(authentication.getPrincipal());
    if (principal == null || principal.isBlank()) {
      return Optional.empty();
    }

    UUID userId;
    try {
      userId = UUID.fromString(principal);
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }

    String role = authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .filter(authority -> authority != null && authority.startsWith("ROLE_"))
        .map(authority -> authority.substring("ROLE_".length()))
        .findFirst()
        .orElse("PATIENT");

    return Optional.of(new CurrentUser(userId, role));
  }
}
