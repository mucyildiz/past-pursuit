package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameEvent {
  private GameEventType eventType;
  private User user;
  private String data;
  private String gameCode;
  private long timestamp;
}
