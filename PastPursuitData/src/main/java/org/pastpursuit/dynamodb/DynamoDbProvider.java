package org.pastpursuit.dynamodb;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoDbProvider {

  private DynamoDbProvider() {
  }

  private static DynamoDbClient client;

  public static DynamoDbClient getClient() {
    if (client == null) {
      client = DynamoDbClient.builder().region(Region.US_EAST_2).credentialsProvider(DefaultCredentialsProvider.create()).build();
    }
    return client;
  }
}
