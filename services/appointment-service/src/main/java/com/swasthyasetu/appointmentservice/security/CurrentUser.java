package com.swasthyasetu.appointmentservice.security;

import java.util.UUID;
import lombok.Value;

@Value
public class CurrentUser {
  UUID id;
  String role;
}
