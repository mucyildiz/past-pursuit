package org.pastpursuit;


import org.pastpursuit.dynamodb.DynamoDbProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

public class ResultRepository {
  private final DynamoDbClient dynamoDbClient;

  public ResultRepository() {
    this.dynamoDbClient = DynamoDbProvider.getClient();
  }

  public ResultRepository(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  public Map<String, AttributeValue> getResult(String partitionKey) {
    GetItemRequest getItemRequest = GetItemRequest.builder()
      .tableName(DynamoDbProvider.DB_NAME)
      .key(Map.of(DynamoDbProvider.PARTITION_KEY, AttributeValue.fromS(partitionKey)))
      .build();
    GetItemResponse response = dynamoDbClient.getItem(getItemRequest);
    return response.item();
  }

  public void save(ImmutableGameResult gameResult) {
    GetItemRequest getItemRequest = GetItemRequest.builder()
      .tableName(DynamoDbProvider.DB_NAME)
      .key(Map.of(
        DynamoDbProvider.PARTITION_KEY,
        AttributeValue.fromS(gameResult.getPartitionKey())))
      .build();
    GetItemResponse response = dynamoDbClient.getItem(getItemRequest);

    int numWins = 0;
    int numLosses = 0;
    if (response.hasItem()) {
      numWins = Integer.parseInt(response.item().get("wins").n()) + 1;
      numLosses = Integer.parseInt(response.item().get("losses").n()) + 1;
    }

    if (gameResult.getResult() == GameResult.Result.WIN) {
      numWins += 1;
    } else {
      numLosses += 1;
    }

    Map<String, AttributeValue> values = new HashMap<>();
    values.put(DynamoDbProvider.PARTITION_KEY, AttributeValue.fromS(gameResult.getPartitionKey()));
    values.put("wins", AttributeValue.builder().n(String.valueOf(numWins))
      .build());
    values.put("losses", AttributeValue.builder().n(String.valueOf(numLosses))
      .build());

    PutItemRequest putItemRequest = PutItemRequest.builder()
      .tableName(DynamoDbProvider.DB_NAME)
      .item(values).build();

    dynamoDbClient.putItem(putItemRequest);
  }
}
