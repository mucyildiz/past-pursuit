package org.pastpursuit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
@JsonDeserialize(as = ImmutableGameEvent.class)
@JsonSerialize(as = ImmutableGameEvent.class)
public interface GameEvent {
  GameEventType getEventType();

  ImmutableUser getUser();

  String getData();

  String getGameCode();

  long getTimestamp();
}
