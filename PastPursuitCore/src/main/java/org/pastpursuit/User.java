package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.immutables.value.Value;

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
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
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
}
