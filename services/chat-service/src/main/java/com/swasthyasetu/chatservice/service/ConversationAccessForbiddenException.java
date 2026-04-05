package com.swasthyasetu.chatservice.service;

public class ConversationAccessForbiddenException extends RuntimeException {
  public ConversationAccessForbiddenException(String message) {
    super(message);
  }
}
