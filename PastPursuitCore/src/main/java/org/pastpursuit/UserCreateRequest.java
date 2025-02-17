package org.pastpursuit;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
public interface UserCreateRequest {
  String getName();
}
