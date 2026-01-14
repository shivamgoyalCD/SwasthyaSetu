package com.swasthyasetu.authservice.config;

import com.swasthyasetu.common.security.JwtUtil;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
  @Bean(name = "accessJwtUtil")
  public JwtUtil accessJwtUtil(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.access-ttl:PT15M}") Duration ttl
  ) {
    return new JwtUtil(secret, ttl);
  }

  @Bean(name = "refreshJwtUtil")
  public JwtUtil refreshJwtUtil(
      @Value("${security.jwt.secret}") String secret,
      @Value("${security.jwt.refresh-ttl:P7D}") Duration ttl
  ) {
    return new JwtUtil(secret, ttl);
  }
}
