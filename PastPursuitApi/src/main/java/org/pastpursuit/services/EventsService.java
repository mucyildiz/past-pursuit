package org.pastpursuit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Path("past-pursuit/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventsService extends ParentService {
  private final Logger LOG = LoggerFactory.getLogger(EventsService.class);
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET();
  private static final String API_URL_BASE = "https://api.api-ninjas.com/v1/historicalevents";
  private final Retryer<List<Event>> retryer;


  public EventsService() {
    Properties properties = new Properties();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
      properties.load(input);
    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
    String apiKey = properties.getProperty("API_KEY");
    requestBuilder.header("X-Api-Key", apiKey);
    retryer = RetryerBuilder.<List<Event>>newBuilder().retryIfResult(List::isEmpty).build();
  }

  @GET
  public Response getEventsSync() throws ExecutionException, RetryException {
    List<Event> events = retryer.call(() -> getEvents().join());
    return Response.ok(events).header("Access-Control-Allow-Origin", "http://localhost:5173")
            .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS")
            .header("Access-Control-Allow-Headers", "Content-Type, Authorization")
            .header("Access-Control-Allow-Credentials", "true").build();
  }

  private CompletableFuture<List<Event>> getEvents() {
    int year = new Random().nextInt(-10000, 2024);
    String url = API_URL_BASE + String.format("?year=%s", year);
    HttpRequest request = requestBuilder.uri(URI.create(url)).build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApplyAsync(response -> {
      TypeReference<List<Event>> typeReference = new TypeReference<>() {};
      try {
        return getObjectMapper().readValue(response.body(), typeReference);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
  }
}
