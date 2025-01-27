package org.pastpursuit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
  private String name;
  private String password;
}
