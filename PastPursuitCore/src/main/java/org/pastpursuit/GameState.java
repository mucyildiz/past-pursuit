package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.grizzly.websockets.WebSocket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public class GameState {
  @JsonIgnore
  Set<WebSocket> webSockets = new HashSet<>();

  Set<ImmutableUser> users = new HashSet<>();
  HashMap<String, Integer> playerScores = new HashMap<>();
  HashMap<String, GuessMeta> currentGuesses = new HashMap<>();
  CurrentGameState currentState;
  String gameCode;
  Optional<HistoricalEvent> currentEvent = Optional.empty();

  public void addUserToGame(ImmutableUser user) {
    users.add(user);
    playerScores.put(user.getId(), 0);
  }

  @Override
  public String toString() {
    return "GameState{" + "users=" + users + ", playerScores=" + playerScores + ", currentGuesses=" + currentGuesses + ", currentState=" + currentState + ", gameCode='" + gameCode + '\'' + ", currentEvent=" + currentEvent + '}';
  }
}
