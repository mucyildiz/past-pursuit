package org.pastpursuit.dynamodb;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

public class DynamoDbProvider {
  public static final String PARTITION_KEY = "pp_partition_key";
  public static final String DB_NAME = "ppdb";

  private DynamoDbProvider() {
  }

  private static DynamoDbClient client;

  public static DynamoDbClient getClient() {
    if (client == null) {
      String localEndpoint = System.getenv("LOCAL_DYNAMODB_ENDPOINT");
      DynamoDbClientBuilder builder = DynamoDbClient.builder()
        .region(Region.US_EAST_2)
        .credentialsProvider(DefaultCredentialsProvider.create());

      if (localEndpoint != null && !localEndpoint.isEmpty()) {
        builder.endpointOverride(URI.create(localEndpoint));
      }

      client = builder.build();
    }
    return client;
  }
}
