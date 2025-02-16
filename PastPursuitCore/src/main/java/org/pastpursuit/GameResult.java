package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameResult {

  private String userId;

  private String opponentId;

  private Result result;

  public enum Result {
    WIN, LOSS
  }

  @JsonIgnore
  public String getPartitionKey() {
    return String.format("RESULT#%s#%s", userId, opponentId);
  }
}
