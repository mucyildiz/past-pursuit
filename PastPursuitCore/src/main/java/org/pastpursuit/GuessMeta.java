package org.pastpursuit;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@Value.Style(init = "set*")
public interface GuessMeta {
  // no guess if player ran out of time
  Optional<Integer> getGuess();

  long getTimestamp();
}
