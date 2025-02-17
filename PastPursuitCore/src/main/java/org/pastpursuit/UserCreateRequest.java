package org.pastpursuit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "set*")
@JsonSerialize(as = ImmutableUserCreateRequest.class)
@JsonDeserialize(as = ImmutableUserCreateRequest.class)
public interface UserCreateRequest {
  String getName();

  String getEmail();
}
