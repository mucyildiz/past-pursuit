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

  public void save(GameResult gameResult) {
    GetItemRequest getItemRequest = GetItemRequest.builder().tableName("ppdb").key(Map.of("pp_partition_key", AttributeValue.fromS(gameResult.getPartitionKey()))).build();
    GetItemResponse response = dynamoDbClient.getItem(getItemRequest);
    int numWins = 0;
    int numLosses = 0;
    if (response.hasItem()) {
      if (gameResult.getResult().equals(GameResult.Result.WIN)) {
        numWins = Integer.parseInt(response.item().get("wins").n()) + 1;
      } else {
        numLosses = Integer.parseInt(response.item().get("losses").n()) + 1;
      }
    }

    Map<String, AttributeValue> values = new HashMap<>();
    values.put("pp_partition_key", AttributeValue.fromS(gameResult.getPartitionKey()));
    values.put("wins", AttributeValue.builder().n(String.valueOf(numWins)).build());
    values.put("losses", AttributeValue.builder().n(String.valueOf(numLosses)).build());

    PutItemRequest putItemRequest = PutItemRequest.builder().tableName("ppdb").item(values).build();
    dynamoDbClient.putItem(putItemRequest);
  }
}
