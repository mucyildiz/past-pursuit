package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.immutables.value.Value;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Value.Immutable
@Value.Style(init = "set*")
public abstract class User implements DynamoDbRow {

  public abstract String getId();

  public abstract String getName();

  public abstract String getEmail();

  public abstract Integer getWins();

  public abstract Integer getLosses();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    if (getId() == null || user.getId() == null) {
      return Objects.equals(getName(), user.getName());
    }
    return Objects.equals(getId(), user.getId());
  }

  @Override
  public int hashCode() {
    if (getId() == null) {
      return Objects.hash(getName());
    }
    return Objects.hash(getId());
  }

  @JsonIgnore
  public String getPartitionKey() {
    return String.format("USER#%s", getId());
  }

  @JsonIgnore
  @Value.Derived
  public Map<String, AttributeValue> getDynamoDbRow() {
    Map<String, AttributeValue> row = new HashMap<>();
    row.put("pp_partition_key", AttributeValue.builder()
      .s(getPartitionKey()).build());
    row.put("name", AttributeValue.fromS(getName()));
    row.put("email", AttributeValue.fromS(getEmail()));
    row.put("wins", AttributeValue.fromN(String.valueOf(getWins())));
    row.put("losses", AttributeValue.fromN(String.valueOf(getLosses())));
    return row;
  }

  @JsonIgnore
  public static ImmutableUser fromDynamoDbRow(Map<String, AttributeValue> ddbRow) {
    return ImmutableUser.builder()
      .setId(ddbRow.get("pp_partition_key").s().replace("USER#", ""))
      .setName(ddbRow.get("name").s())
      .setEmail(ddbRow.get("email").s())
      .setWins(Integer.parseInt(ddbRow.get("wins").n()))
      .setLosses(Integer.parseInt(ddbRow.get("losses").n()))
      .build();
  }
}
