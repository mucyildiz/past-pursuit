package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
public class GameState {
  Set<User> users = new HashSet<>();
  HashMap<Long, Integer> playerScores = new HashMap<>();
  HashMap<Long, GuessMeta> currentGuesses = new HashMap<>();
  CurrentGameState currentState;
  String gameCode;
  Optional<HistoricalEvent> currentEvent = Optional.empty();

  public void addUserToGame(User user) {
    users.add(user);
    playerScores.put(user.getId(), 0);
  }

  @Override
  public String toString() {
    return "GameState{" + "users=" + users + ", playerScores=" + playerScores + ", currentGuesses=" + currentGuesses + ", currentState=" + currentState + ", gameCode='" + gameCode + '\'' + ", currentEvent=" + currentEvent + '}';
  }
}
