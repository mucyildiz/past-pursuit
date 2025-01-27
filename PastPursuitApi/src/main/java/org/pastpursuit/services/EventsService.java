package org.pastpursuit.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.HistoricalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Path("past-pursuit/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventsService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final Logger LOG = LoggerFactory.getLogger(EventsService.class);
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().GET();
  private static final String API_URL_BASE = "https://api.api-ninjas.com/v1/historicalevents";
  private final Retryer<List<HistoricalEvent>> retryer;


  public EventsService() {
    Properties properties = new Properties();
    try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
      properties.load(input);
    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
    String apiKey = properties.getProperty("API_KEY");
    requestBuilder.header("X-Api-Key", apiKey);
    retryer = RetryerBuilder.<List<HistoricalEvent>>newBuilder().retryIfResult(List::isEmpty).build();
  }

  @GET
  public Response getEventsSync() throws ExecutionException, RetryException {
    List<HistoricalEvent> historicalEvents = getEvents();
    return Response.ok(historicalEvents).header("Access-Control-Allow-Origin", "http://localhost:5173").header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS").header("Access-Control-Allow-Headers", "Content-Type, Authorization").header("Access-Control-Allow-Credentials", "true").build();
  }

  public HistoricalEvent getRandomEvent() {
    List<HistoricalEvent> historicalEvents = getEvents();
    LOG.info("Returned events: {}", historicalEvents);
    return historicalEvents.get(new Random().nextInt(historicalEvents.size()));
  }

  private List<HistoricalEvent> getEvents() {
    java.nio.file.Path filePath = Paths.get("/Users/alperenyildiz/Desktop/PastPursuit/PastPursuitApi/src/main/resources/formatted-events.json");
    File file = filePath.toFile();

    try (FileReader fileReader = new FileReader(file)) {
      Map<Integer, List<HistoricalEvent>> eventsByYear = OBJECT_MAPPER.readValue(fileReader, new TypeReference<>() {
      });
      List<Integer> years = new ArrayList<>(eventsByYear.keySet());
      List<HistoricalEvent> historicalEvents = eventsByYear.get(years.get(new Random().nextInt(years.size())));
      if (historicalEvents == null) {
        LOG.error("No events found");
        return Collections.emptyList();
      }
      return historicalEvents;
    } catch (IOException e) {
      LOG.error("Error reading events from file", e);
      return Collections.emptyList();
    }

  }
}
