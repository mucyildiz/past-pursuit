package org.pastpursuit.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.pastpursuit.HistoricalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Path("past-pursuit/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventsService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final Logger LOG = LoggerFactory.getLogger(EventsService.class);
  private final Map<Integer, List<HistoricalEvent>> eventsByYear;
  private final List<Integer> years;
  private final Random random = new Random();

  public EventsService() {
    InputStream inputStream = getClass().getClassLoader()
      .getResourceAsStream("formatted-events.json");
    if (inputStream == null) {
      throw new IllegalStateException("formatted-events.json not found in the classpath");
    }
    try {
      eventsByYear = OBJECT_MAPPER.readValue(inputStream, new TypeReference<>() {
      });
    } catch (IOException e) {
      LOG.error("Error reading events from file", e);
      throw new IllegalStateException("Error reading events.");
    }
    years = new ArrayList<>(eventsByYear.keySet());
  }

  public HistoricalEvent getRandomEvent() {
    Integer randomYear = years.get(random.nextInt(years.size()));
    List<HistoricalEvent> historicalEvents = eventsByYear.get(randomYear);
    if (historicalEvents == null) {
      LOG.error("No events found for year {}", randomYear);
      throw new IllegalStateException("No events found");
    }
    return historicalEvents.get(random.nextInt(historicalEvents.size()));

  }
}
