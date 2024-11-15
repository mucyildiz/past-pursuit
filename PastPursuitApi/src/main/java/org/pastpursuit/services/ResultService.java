package org.pastpursuit.services;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.pastpursuit.GameResult;
import org.pastpursuit.ResultRepository;

@Path("past-pursuit/results")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ResultService extends ParentService {
  private final ResultRepository resultRepository = new ResultRepository();

  @POST
  public GameResult createResult(GameResult gameResult) {
    resultRepository.save(gameResult);
    return gameResult;
  }
}
