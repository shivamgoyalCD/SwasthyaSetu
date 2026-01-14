package com.swasthyasetu.apigateway;

import com.swasthyasetu.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {
  private static final String AUTH_PREFIX = "Bearer ";

  private final JwtUtil jwtUtil;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();
    if (isPublicPath(path)) {
      return chain.filter(exchange);
    }

    String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith(AUTH_PREFIX)) {
      return unauthorized(exchange);
    }

    String token = authHeader.substring(AUTH_PREFIX.length());
    if (!jwtUtil.validateToken(token)) {
      return unauthorized(exchange);
    }

    return chain.filter(exchange);
  }

  private boolean isPublicPath(String path) {
    return "/health".equals(path) || "/auth".equals(path) || path.startsWith("/auth/");
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
    return exchange.getResponse().setComplete();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 5;
  }
}
