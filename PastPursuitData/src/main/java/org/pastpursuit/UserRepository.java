package org.pastpursuit;

import org.pastpursuit.dynamodb.DynamoDbProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;

public class UserRepository {
  private final DynamoDbClient dynamoDbClient;

  public UserRepository() {
    this.dynamoDbClient = DynamoDbProvider.getClient();
  }

  public User save(User user) {
    PutItemRequest putItemRequest = PutItemRequest.builder().tableName("ppdb").item(Map.of("pp_partition_key", AttributeValue.fromS(user.getPartitionKey()))).build();
    dynamoDbClient.putItem(putItemRequest);
    return user;
  }
}
