package org.pastpursuit;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
public interface HistoricalEvent {
  Integer getYear();

  Integer getMonth();

  Integer getDay();

  String getEvent();
}
