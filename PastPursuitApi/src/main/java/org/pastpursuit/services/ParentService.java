package org.pastpursuit.services;

import com.fasterxml.jackson.databind.ObjectMapper;
public class ParentService {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
