package org.pastpursuit;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.net.URI;
import java.util.Map;

public class ResultRepositoryTest {


  private final DynamoDbClient dynamoDbClient = DynamoDbClient.builder()
    .endpointOverride(URI.create("http://localhost:8000"))
    .build();
  private final ResultRepository resultRepository = new ResultRepository(dynamoDbClient);

  @Test
  public void itSavesGameResult() {
    ImmutableGameResult gameResult = ImmutableGameResult.builder()
      .setUserId("userId")
      .setOpponentId("oppId")
      .setResult(GameResult.Result.WIN)
      .build();
    resultRepository.save(gameResult);
    Map<String, AttributeValue> result = resultRepository.getResult(gameResult.getPartitionKey());
    int wins = Integer.parseInt(result.get("wins").n());
    assert wins == 3;
  }
}
