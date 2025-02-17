package org.pastpursuit;

import org.pastpursuit.dynamodb.DynamoDbProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class UserRepository {
  private final DynamoDbClient dynamoDbClient;

  public UserRepository() {
    this.dynamoDbClient = DynamoDbProvider.getClient();
  }

  public ImmutableUser save(ImmutableUser user) {
    PutItemRequest putItemRequest = PutItemRequest.builder().tableName("ppdb")
      .item(user.getDynamoDbRow())
      .build();
    
    dynamoDbClient.putItem(putItemRequest);
    return user;
  }
}
