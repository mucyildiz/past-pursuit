package org.pastpursuit.services;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.pastpursuit.User;
import org.pastpursuit.UserRepository;

@Path("past-pursuit/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService extends ParentService {
  private final UserRepository userRepository = new UserRepository();

  @POST
  public User createUser(User user) {
    userRepository.save(user);
    return user;
  }
}
