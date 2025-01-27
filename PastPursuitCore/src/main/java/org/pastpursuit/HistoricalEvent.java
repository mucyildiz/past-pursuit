package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoricalEvent {
  private Integer year;
  private Integer month;
  private Integer day;
  private String event;
}
