package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface DynamoDbRow {
  @JsonIgnore
  String getPartitionKey();
}
