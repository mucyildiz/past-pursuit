package org.pastpursuit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Strings;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.pastpursuit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SocketsService extends WebSocketApplication {
  private static final Logger LOG = LoggerFactory.getLogger(SocketsService.class);
  private static final int NUM_PLAYERS_PER_GAME = 2;
  private static final int WINNING_SCORE = 4;

  private final ObjectMapper objectMapper = new ObjectMapper();
  // Game state stored in a concurrent map
  private final ConcurrentHashMap<String, GameState> gameStates = new ConcurrentHashMap<>();

  // Services (assumed to be lightweight singletons or stateless)
  private final ResultService resultService = new ResultService();
  private final UserService userService = new UserService();
  private final EventsService eventsService = new EventsService();

  public SocketsService() {
    // Register Jackson module for JDK8 support
    objectMapper.registerModule(new Jdk8Module());
  }

  @Override
  public void onConnect(WebSocket socket) {
    LOG.info("WebSocket connected. Hi.");
    super.onConnect(socket);
  }

  @Override
  public void onClose(WebSocket socket, DataFrame frame) {
    LOG.info("WebSocket closed. DataFrame: {}", frame);
    // Remove the socket from any game state that contains it
    gameStates.values().forEach(gameState -> {
      if (gameState.getWebSockets().contains(socket)) {
        gameStates.remove(gameState.getGameCode());
      }
    });
    super.onClose(socket, frame);
  }

  @Override
  public void onMessage(WebSocket socket, String message) {
    LOG.info("Message received: {}", message);
    GameEvent gameEvent;
    try {
      gameEvent = objectMapper.readValue(message, GameEvent.class);
    } catch (JsonProcessingException e) {
      LOG.error("Failed to parse message: {}", message, e);
      throw new RuntimeException(e);
    }
    switch (gameEvent.getEventType()) {
      case PLAYER_JOINED -> handleJoin(gameEvent, socket);
      case GUESS -> handleGuess(gameEvent);
      case ROUND_START -> handleRoundStart(gameEvent);
      case REMATCH -> handleRematch(gameEvent);
      case PLAYER_LEFT -> handleGameExit(gameEvent);
      case REMATCH_PROPOSAL -> handleRematchProposal(gameEvent);
      default -> LOG.warn("Unhandled event type: {}", gameEvent.getEventType());
    }
  }

  @Override
  public boolean onError(WebSocket socket, Throwable t) {
    LOG.error("NOOOOOOOOOOOOOOOO!!!! WebSocket error on {}: ", socket, t);
    return false;
  }


  // --- Private helper methods below ---
  private void handleRematchProposal(GameEvent gameEvent) {
    GameState gameState = gameStates.get(gameEvent.getGameCode());
    if (gameState.getCurrentState().equals(CurrentGameState.REMATCH_PROPOSED)) {
      handleRematch(gameEvent);
    } else {
      gameState.setCurrentState(CurrentGameState.REMATCH_PROPOSED);
      broadcastGameState(gameState);
    }
  }

  private void handleGameExit(GameEvent gameEvent) {
    GameState gameState = gameStates.get(gameEvent.getGameCode());
    gameState.setCurrentState(CurrentGameState.GAME_EXIT);
    broadcastGameState(gameState);
    gameStates.remove(gameEvent.getGameCode());
  }

  private void handleRematch(GameEvent gameEvent) {
    GameState gameState = gameStates.get(gameEvent.getGameCode());
    gameState.setCurrentGuesses(new HashMap<>());
    HashMap<String, Integer> resetScores = new HashMap<>();
    for (Map.Entry<String, Integer> score : gameState.getPlayerScores().entrySet()) {
      resetScores.put(score.getKey(), 0);
    }
    gameState.setPlayerScores(resetScores);
    gameState.setCurrentState(CurrentGameState.GAME_START);
    broadcastGameState(gameState);
  }

  private void handleRoundStart(GameEvent roundStartEvent) {
    LOG.info("Round start event received: {}", roundStartEvent);
    if (!roundStartEvent.getEventType().equals(GameEventType.ROUND_START)) {
      LOG.error("Expected ROUND_START event, but got: {}", roundStartEvent.getEventType());
    }
    GameState gameState = gameStates.get(roundStartEvent.getGameCode());
    if (gameState.getCurrentState().equals(CurrentGameState.WAITING_FOR_GUESSES) || gameState.getCurrentState().equals(CurrentGameState.TIMER_START)) {
      LOG.info("Game {} already waiting for guesses.", roundStartEvent.getGameCode());
      return;
    }
    gameState.setCurrentGuesses(new HashMap<>());
    gameState.setCurrentEvent(Optional.of(eventsService.getRandomEvent()));
    gameState.setCurrentState(CurrentGameState.WAITING_FOR_GUESSES);
    broadcastGameState(gameState);
  }

  private void handleJoin(GameEvent joinEvent, WebSocket socket) {
    String gameCode = joinEvent.getGameCode();
    if (!gameStates.containsKey(gameCode)) {
      createInitialGameState(joinEvent, socket);
    } else {
      addUserToExistingGame(joinEvent, socket);
    }
    GameState gameState = gameStates.get(gameCode);
    gameState.getWebSockets().add(socket);
  }

  private void createInitialGameState(GameEvent joinEvent, WebSocket socket) {
    GameState initialGameState = new GameState();
    initialGameState.setGameCode(joinEvent.getGameCode());
    initialGameState.addUserToGame(joinEvent.getUser());
    initialGameState.setCurrentState(CurrentGameState.WAITING_FOR_PLAYERS);
    initialGameState.getWebSockets().add(socket);
    gameStates.put(joinEvent.getGameCode(), initialGameState);
    broadcastGameState(initialGameState);
    LOG.info("Game {} created with user {}", joinEvent.getGameCode(), joinEvent.getUser());
  }

  private void addUserToExistingGame(GameEvent joinEvent, WebSocket socket) {
    GameState existingGame = gameStates.get(joinEvent.getGameCode());
    existingGame.addUserToGame(joinEvent.getUser());
    if (existingGame.getUsers().size() == NUM_PLAYERS_PER_GAME) {
      existingGame.setCurrentState(CurrentGameState.GAME_START);
    }
    existingGame.getWebSockets().add(socket);
    broadcastGameState(existingGame);
    LOG.info("User {} joined game {}", joinEvent.getUser(), joinEvent.getGameCode());
  }

  private void handleGuess(GameEvent guessEvent) {
    GuessMeta guessMeta = new GuessMeta();
    // this happens if someone ran out of time
    if (Strings.isNullOrEmpty(guessEvent.getData())) {
      guessMeta.setGuess(Optional.empty());
    } else {
      guessMeta.setGuess(Optional.of(Integer.parseInt(guessEvent.getData())));
    }
    guessMeta.setTimestamp(guessEvent.getTimestamp());

    GameState gameState = gameStates.get(guessEvent.getGameCode());
    gameState.getCurrentGuesses().put(guessEvent.getUser().getId(), guessMeta);

    boolean allGuessesIn = gameState.getCurrentGuesses().size() == gameState.getUsers().size();
    if (allGuessesIn) {
      completeRound(gameState, guessEvent);
    } else {
      gameState.setCurrentState(CurrentGameState.TIMER_START);
      broadcastGameState(gameState);
    }
  }

  private Optional<String> getRoundWinnerId(GameState gameState) {
    // Get the correct answer for this round
    HistoricalEvent currentRoundEvent = gameState.getCurrentEvent().orElseThrow(() -> new RuntimeException("No event found for this round."));
    int correctYear = currentRoundEvent.getYear();

    // Validate that all players have made a guess before proceeding
    if (gameState.getCurrentGuesses().size() != NUM_PLAYERS_PER_GAME) {
      throw new RuntimeException("Not all guesses are in yet. This shouldn't be called at this time.");
    }

    // We'll keep track of the best (smallest) distance. If multiple guesses
    // have the same distance, we use the earliest guess timestamp.
    int bestDistance = Integer.MAX_VALUE;
    long earliestTimestamp = Long.MAX_VALUE;
    String bestUserId = null;

    // Iterate through each guess
    for (Map.Entry<String, GuessMeta> entry : gameState.getCurrentGuesses().entrySet().stream().filter(e -> e.getValue().getGuess().isPresent()).toList()) {
      String userId = entry.getKey();
      GuessMeta guessMeta = entry.getValue();

      int guessValue = guessMeta.getGuess().orElseThrow();
      long guessTimestamp = guessMeta.getTimestamp();

      // Calculate how far off this guess is from the correctYear
      int currentDistance = Math.abs(guessValue - correctYear);

      // Compare with the best distance so far
      if (currentDistance < bestDistance) {
        // This guess is closer than any previous guess
        bestDistance = currentDistance;
        earliestTimestamp = guessTimestamp;
        bestUserId = userId;
      } else if (currentDistance == bestDistance && guessTimestamp < earliestTimestamp) {
        earliestTimestamp = guessTimestamp;
        bestUserId = userId;
      }

    }

    // Return the userId of the winning guess if it exists
    return Optional.ofNullable(bestUserId);
  }

  private void completeRound(GameState gameState, GameEvent guessEvent) {
    Optional<String> maybeWinnerId = getRoundWinnerId(gameState);
    maybeWinnerId.ifPresent(winnerId -> {
      int winnerNewScore = gameState.getPlayerScores().get(winnerId) + 1;
      gameState.getPlayerScores().put(winnerId, winnerNewScore);
      if (winnerNewScore == WINNING_SCORE) {
        completeGame(gameState, winnerId);
        return;
      }
      gameState.setCurrentState(CurrentGameState.ROUND_OVER);
      broadcastGameState(gameState);
    });
  }

  private void completeGame(GameState gameState, String winnerId) {
    gameState.getUsers().forEach(user -> {
      if (user.getId().equals(winnerId)) {
        user.setWins(user.getWins() + 1);
        userService.updateUser(user);
      } else {
        user.setLosses(user.getLosses() + 1);
        userService.updateUser(user);
      }
    });

    gameState.setCurrentState(CurrentGameState.GAME_OVER);
    broadcastGameState(gameState);
    resultService.persistResult(gameState);
  }

  private void broadcastGameState(GameState gameState) {
    try {
      String json = objectMapper.writeValueAsString(gameState);
      for (WebSocket socket : gameState.getWebSockets()) {
        if (socket.isConnected()) {
          socket.send(json);
        }
      }
    } catch (JsonProcessingException e) {
      LOG.error("Failed to broadcast game state", e);
    }
  }
}
