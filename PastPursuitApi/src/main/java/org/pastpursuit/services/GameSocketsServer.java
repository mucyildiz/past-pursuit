package org.pastpursuit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Strings;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.pastpursuit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GameSocketsServer extends WebSocketServer {
  private static final Logger LOG = LoggerFactory.getLogger(GameSocketsServer.class);
  private static final int NUM_PLAYERS_PER_GAME = 2;
  private static final int WINNING_SCORE = 4;

  private final ObjectMapper objectMapper = new ObjectMapper();

  // state-of-the-art database technology for storing game states
  private final ConcurrentHashMap<String, GameState> gameStates;

  private final ResultService resultService;
  private final UserService userService;
  private final EventsService eventsService;

  public GameSocketsServer(int port) {
    super(new InetSocketAddress(port));
    resultService = new ResultService();
    userService = new UserService();
    eventsService = new EventsService();
    gameStates = new ConcurrentHashMap<>();
    objectMapper.registerModule(new Jdk8Module());
  }

  @Override
  public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
    LOG.info("Oh shit, what's good {}", webSocket.getRemoteSocketAddress());
  }

  @Override
  public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
    LOG.info("Connection closed. Code: {}, reason: {}", code, reason);
    for (GameState gameState : gameStates.values()) {
      if (gameState.getWebSockets().contains(webSocket)) {
        gameStates.remove(gameState.getGameCode());
      }
    }
    webSocket.close();
  }

  @Override
  public void onMessage(WebSocket webSocket, String message) {
    GameEvent gameEvent;
    try {
      gameEvent = objectMapper.readValue(message, GameEvent.class);
    } catch (JsonProcessingException e) {
      LOG.error("What the fuck am I supposed to do with {}", message, e);
      throw new RuntimeException(e);
    }
    switch (gameEvent.getEventType()) {
      case PLAYER_JOINED -> handleJoin(gameEvent, webSocket);
      case GUESS -> handleGuess(gameEvent);
      case ROUND_START -> handleRoundStart(gameEvent);
      case REMATCH -> handleRematch(gameEvent);
      case PLAYER_LEFT -> handleGameExit(gameEvent);
      case REMATCH_PROPOSAL -> handleRematchProposal(gameEvent);
    }
  }

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
    HashMap<Long, Integer> resetScores = new HashMap<>();
    for (Map.Entry<Long, Integer> score : gameState.getPlayerScores().entrySet()) {
      resetScores.put(score.getKey(), 0);
    }
    gameState.setPlayerScores(resetScores);
    gameState.setCurrentState(CurrentGameState.GAME_START);
    broadcastGameState(gameState);
  }

  private void handleRoundStart(GameEvent roundStartEvent) {
    LOG.info("Round start event received: {}", roundStartEvent);
    if (!roundStartEvent.getEventType().equals(GameEventType.ROUND_START)) {
      LOG.error("Event must be ROUND_START, but it was {}", roundStartEvent.getEventType());
    }

    GameState gameState = gameStates.get(roundStartEvent.getGameCode());

    if (gameState.getCurrentState().equals(CurrentGameState.WAITING_FOR_GUESSES) || gameState.getCurrentState().equals(CurrentGameState.TIMER_START)) {
      LOG.info("Game with code {} already waiting for guesses, not refreshing event.", roundStartEvent.getGameCode());
      return;
    }

    gameState.setCurrentGuesses(new HashMap<>());
    gameState.setCurrentEvent(Optional.of(eventsService.getRandomEvent()));
    gameState.setCurrentState(CurrentGameState.WAITING_FOR_GUESSES);
    broadcastGameState(gameState);
  }

  private void handleJoin(GameEvent joinEvent, WebSocket webSocket) {
    String gameCode = joinEvent.getGameCode();
    if (!gameStates.containsKey(gameCode)) {
      createInitialGameState(joinEvent, webSocket);
    } else {
      addUserToExistingGame(joinEvent, webSocket);
    }
    GameState gameState = gameStates.get(gameCode);
    gameState.getWebSockets().add(webSocket);
  }

  private void createInitialGameState(GameEvent joinEvent, WebSocket webSocket) {
    GameState initialGameState = new GameState();
    initialGameState.setGameCode(joinEvent.getGameCode());
    initialGameState.addUserToGame(joinEvent.getUser());
    initialGameState.setCurrentState(CurrentGameState.WAITING_FOR_PLAYERS);
    initialGameState.getWebSockets().add(webSocket);
    gameStates.put(joinEvent.getGameCode(), initialGameState);
    broadcastGameState(initialGameState);
    LOG.info("Game {} created with user {}", joinEvent.getGameCode(), joinEvent.getUser());
  }

  private void addUserToExistingGame(GameEvent joinEvent, WebSocket webSocket) {
    GameState existingGame = gameStates.get(joinEvent.getGameCode());
    existingGame.addUserToGame(joinEvent.getUser());

    if (existingGame.getUsers().size() == NUM_PLAYERS_PER_GAME) {
      existingGame.setCurrentState(CurrentGameState.GAME_START);
    }
    existingGame.getWebSockets().add(webSocket);
    broadcastGameState(existingGame);
    LOG.info("User {} joined game {}", joinEvent.getUser(), joinEvent.getGameCode());
    LOG.info("Game state: {}", existingGame);
  }

  @Override
  public void onError(WebSocket webSocket, Exception e) {
    LOG.error("NOOOOOOOOOOOOOOOOO, {} messed up.", webSocket.getRemoteSocketAddress(), e);
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

  private Optional<Long> getRoundWinnerId(GameState gameState) {
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
    Long bestUserId = null;

    // Iterate through each guess
    for (Map.Entry<Long, GuessMeta> entry : gameState.getCurrentGuesses().entrySet().stream().filter(e -> e.getValue().getGuess().isPresent()).toList()) {
      Long userId = entry.getKey();
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
    Optional<Long> maybeWinnerId = getRoundWinnerId(gameState);
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

  private void completeGame(GameState gameState, Long winnerId) {
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


  @Override
  public void onStart() {
    LOG.info("We out here. Server started on port {}", getPort());
    setConnectionLostTimeout(0);
    setConnectionLostTimeout(30);
    setReuseAddr(true);
  }

  private void broadcastGameState(GameState gameState) {
    try {
      for (WebSocket socket : gameState.getWebSockets()) {
        if (socket.isOpen()) {
          socket.send(objectMapper.writeValueAsString(gameState));
        }
      }
    } catch (JsonProcessingException e) {
      LOG.error("NOOOOOOOOOOOOOOOOO. Failed to broadcast game state", e);
    }
  }
}
