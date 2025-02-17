package org.pastpursuit;

import org.immutables.value.Value;

@Value.Immutable
public interface HistoricalEvent {
  Integer getYear();

  Integer getMonth();

  Integer getDay();

  String getEvent();
}
