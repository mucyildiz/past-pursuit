package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class GuessMeta {
  // no guess if player ran out of time
  private Optional<Integer> guess;
  private long timestamp;
}
