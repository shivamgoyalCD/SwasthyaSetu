package com.swasthyasetu.userservice.config;

import com.swasthyasetu.common.security.JwtAuthenticationFilter;
import com.swasthyasetu.common.security.JwtUtil;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
  @Bean
  public JwtUtil jwtUtil(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.ttl:PT15M}") Duration ttl
  ) {
    return new JwtUtil(secret, ttl);
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
    return new JwtAuthenticationFilter(jwtUtil);
  }
}
