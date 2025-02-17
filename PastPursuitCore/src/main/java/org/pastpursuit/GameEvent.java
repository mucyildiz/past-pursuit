package org.pastpursuit;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
public interface GameEvent {
  GameEventType getEventType();

  ImmutableUser getUser();

  String getData();

  String getGameCode();

  long getTimestamp();
}
