package org.pastpursuit.services;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.User;
import org.pastpursuit.UserCreateRequest;
import org.pastpursuit.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Path("past-pursuit/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {
  private final Logger LOG = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository = new UserRepository();

  @GET
  @Path("health")
  public Response health() {
    return Response.ok("Healthy").build();
  }

  @POST
  public Response createUser(UserCreateRequest user) {
    LOG.info("Creating user: {}", user);
    User newUser = new User();
    newUser.setId(UUID.randomUUID().toString());
    newUser.setName(user.getName());
    newUser.setEmail("placeholder@email.com");
    newUser.setWins(0);
    newUser.setLosses(0);
    User createdUser = userRepository.save(newUser);
    Response.ResponseBuilder response = Response.status(Response.Status.CREATED).entity(createdUser);
    response.header("Access-Control-Allow-Origin", "https://pastpursuit.io");
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
    response.header("Access-Control-Allow-Origin", "https://pastpursuit.io");
    response.header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
    response.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
    response.header("Access-Control-Allow-Credentials", "true");
    response.header("Access-Control-Allow-Private-Network", "true");

    return response.build();
  }

  public User updateUser(User user) {
    userRepository.save(user);
    return user;
  }
}
