package org.pastpursuit.services;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.pastpursuit.*;

@Path("past-pursuit/results")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResultService {
  private static final ResultRepository resultRepository = new ResultRepository();

  @POST
  public void persistResult(GameState gameState) {
    for (User user : gameState.getUsers()) {
      String opponentId = gameState.getUsers()
        .stream()
        .filter(u -> !u.equals(user))
        .findFirst()
        .orElseThrow()
        .getId();

      GameResult.Result result =
        gameState.getPlayerScores()
          .get(user.getId())
          .equals(
            gameState.getPlayerScores().values().stream()
              .max(Integer::compareTo)
              .orElseThrow()) ?
        GameResult.Result.WIN :
        GameResult.Result.LOSS;

      resultRepository.save(
        ImmutableGameResult.builder()
          .setResult(result)
          .setUserId(user.getId())
          .setOpponentId(opponentId)
          .build());
    }
  }
}
