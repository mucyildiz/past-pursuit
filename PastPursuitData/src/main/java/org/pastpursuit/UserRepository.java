package org.pastpursuit;

import org.pastpursuit.dynamodb.DynamoDbProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserRepository {
  private static final Logger LOG = LoggerFactory.getLogger(UserRepository.class);
  private final DynamoDbClient dynamoDbClient;

  public UserRepository() {
    this.dynamoDbClient = DynamoDbProvider.getClient();
  }

  public ImmutableUser save(ImmutableUser user) {
    if (user.getEmail().isEmpty()) {
      throw new IllegalArgumentException("User email is required for saving in the database");
    }
    PutItemRequest putItemRequest = PutItemRequest.builder()
      .tableName(DynamoDbProvider.DB_NAME)
      .item(user.getDynamoDbRow())
      .build();

    dynamoDbClient.putItem(putItemRequest);
    return user;
  }

  public Optional<ImmutableUser> getByEmail(String email) {
    Map<String, AttributeValue> expressionValues = new HashMap<>();
    expressionValues.put(":email", AttributeValue.builder().s(email).build());

    QueryRequest queryRequest = QueryRequest.builder()
      .tableName(DynamoDbProvider.DB_NAME)
      .indexName("email-index")
      .keyConditionExpression("email = :email")
      .expressionAttributeValues(expressionValues)
      .build();

    QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
    if (queryResponse.count() == 0) {
      return Optional.empty();
    }
    if (queryResponse.count() > 1) {
      LOG.error("Multiple users found with email: {}", email);
    }

    Map<String, AttributeValue> ddbRow = queryResponse.items().getFirst();
    return Optional.of(User.fromDynamoDbRow(ddbRow));
  }
}
