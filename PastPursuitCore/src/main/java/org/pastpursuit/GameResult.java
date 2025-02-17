package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
public abstract class GameResult implements DynamoDbRow {

  public abstract String getUserId();

  public abstract String getOpponentId();

  public abstract Result getResult();

  public enum Result {
    WIN, LOSS
  }

  @JsonIgnore
  public String getPartitionKey() {
    return String.format("RESULT#%s#%s", getUserId(), getOpponentId());
  }
}
