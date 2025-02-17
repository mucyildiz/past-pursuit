package org.pastpursuit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
@JsonDeserialize(as = ImmutableHistoricalEvent.class)
@JsonSerialize(as = ImmutableHistoricalEvent.class)
public interface HistoricalEvent {
  Integer getYear();

  Integer getMonth();

  Integer getDay();

  String getEvent();
}
