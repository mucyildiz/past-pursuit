package org.pastpursuit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
@Value.Style(init = "set*")
@JsonDeserialize(as = ImmutableGuessMeta.class)
@JsonSerialize(as = ImmutableGuessMeta.class)
public interface GuessMeta {
  // no guess if player ran out of time
  Optional<Integer> getGuess();

  long getTimestamp();
}
