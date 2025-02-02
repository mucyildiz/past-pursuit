package org.pastpursuit.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.User;
import org.pastpursuit.UserCreateRequest;
import org.pastpursuit.UserRepository;

@Path("past-pursuit/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {
  private final UserRepository userRepository = new UserRepository();

  @POST
  public Response createUser(UserCreateRequest user) {
    User newUser = new User();
    newUser.setName(user.getName());
    newUser.setPassword(user.getPassword());
    newUser.setWins(0);
    newUser.setLosses(0);
    User createdUser = userRepository.save(newUser);
    Response.ResponseBuilder response = Response.status(Response.Status.CREATED).entity(createdUser);
    response.header("Access-Control-Allow-Origin", "https://pastpursuit.io");
    response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.header("Access-Control-Allow-Credentials", "true");

    return response.build();
  }

  @OPTIONS
  public Response handleOptions() {
    Response.ResponseBuilder response = Response.ok();
    response.header("Access-Control-Allow-Origin", "https://pastpursuit.io");
    response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.header("Access-Control-Allow-Credentials", "true");

    return response.build();
  }

  public User updateUser(User user) {
    userRepository.update(user);
    return user;
  }
}
