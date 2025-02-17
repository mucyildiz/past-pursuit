package org.pastpursuit;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.UUID;

class ResultRepositoryTest {
  private final ResultRepository resultRepository = new ResultRepository();

  @Test
  void itSavesGameResult() {
    String userId = UUID.randomUUID().toString();
    String opponentId = UUID.randomUUID().toString();

    ImmutableGameResult gameResult = ImmutableGameResult.builder()
      .setUserId(userId)
      .setOpponentId(opponentId)
      .setResult(GameResult.Result.WIN)
      .build();

    resultRepository.save(gameResult);
    Map<String, AttributeValue> result = resultRepository.getResult(gameResult.getPartitionKey());
    int wins = Integer.parseInt(result.get("wins").n());
    assert wins == 1;
  }
}
