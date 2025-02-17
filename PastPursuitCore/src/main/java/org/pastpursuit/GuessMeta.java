package org.pastpursuit;

import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface GuessMeta {
  // no guess if player ran out of time
  Optional<Integer> getGuess();

  long getTimestamp();
}
