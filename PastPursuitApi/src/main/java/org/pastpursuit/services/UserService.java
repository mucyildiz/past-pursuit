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

    return response.build();
  }

  @OPTIONS
  public Response handleOptions() {
    LOG.info("Handling OPTIONS request");
    return Response.ok().build();
  }

  public void updateUser(ImmutableUser user) {
    userRepository.save(user);
  }

  public Optional<ImmutableUser> getByEmail(String email) {
    return userRepository.getByEmail(email);
  }

  @GET
  @Path("/leaderboard")
  public Response getLeaderboard() {
    return Response.ok().entity(userRepository.getLeadingUsersByNumberOfWins())
      .build();
  }
}
