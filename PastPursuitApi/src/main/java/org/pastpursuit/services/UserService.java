package org.pastpursuit.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.ImmutableUser;
import org.pastpursuit.User;
import org.pastpursuit.UserCreateRequest;
import org.pastpursuit.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Path("past-pursuit/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {
  private final Logger LOG = LoggerFactory.getLogger(UserService.class);
  private static final UserRepository userRepository = new UserRepository();

  @GET
  @Path("health")
  public Response health() {
    return Response.ok("Healthy").build();
  }

  @POST
  public Response createUser(UserCreateRequest user) {
    LOG.info("Creating user: {}", user);
    ImmutableUser newUser = ImmutableUser.builder()
      .setId(UUID.randomUUID().toString())
      .setName(user.getName())
      .setEmail(user.getEmail())
      .setWins(0)
      .setLosses(0)
      .build();

    User createdUser = userRepository.save(newUser);
    Response.ResponseBuilder response = Response.status(Response.Status.CREATED)
      .entity(createdUser);

    String localOrigin = System.getenv("USE_LOCAL_ORIGIN");
    String origin = localOrigin != null ?
                    "http://localhost:5173" :
                    "https://pastpursuit.io";

    response.header("Access-Control-Allow-Origin", origin);
    response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.header("Access-Control-Allow-Credentials", "true");
    response.header("Access-Control-Allow-Private-Network", "true");

    return response.build();
  }

  @OPTIONS
  public Response handleOptions() {
    LOG.info("Handling OPTIONS request");
    Response.ResponseBuilder response = Response.ok();
    String localOrigin = System.getenv("USE_LOCAL_ORIGIN");
    String origin = localOrigin != null ?
                    "http://localhost:5173" :
                    "https://pastpursuit.io";

    response.header("Access-Control-Allow-Origin", origin);
    response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.header("Access-Control-Allow-Credentials", "true");
    response.header("Access-Control-Allow-Private-Network", "true");

    return response.build();
  }

  public void updateUser(ImmutableUser user) {
    userRepository.save(user);
  }

  public Optional<ImmutableUser> getByEmail(String email) {
    return userRepository.getByEmail(email);
  }
}
