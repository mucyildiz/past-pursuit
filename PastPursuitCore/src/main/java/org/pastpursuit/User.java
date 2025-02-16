package org.pastpursuit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class User {

  private String id;

  private String name;

  private String email;

  private Integer wins;

  private Integer losses;

  @Override
  public String toString() {
    return "User{" + "id=" + id + ", name='" + name + '\'' + ", wins=" + wins + ", losses=" + losses + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    if (getId() == null || user.getId() == null) {
      return Objects.equals(getName(), user.getName());
    }
    return Objects.equals(id, user.id);
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
    return String.format("USER#%s", id);
  }
}
