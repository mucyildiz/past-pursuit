package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  private String password;

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
}
