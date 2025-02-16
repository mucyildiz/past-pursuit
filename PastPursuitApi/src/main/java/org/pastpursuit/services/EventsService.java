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
import java.util.*;

@Path("past-pursuit/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventsService {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private final Logger LOG = LoggerFactory.getLogger(EventsService.class);


  public EventsService() {
  }

  public HistoricalEvent getRandomEvent() {
    List<HistoricalEvent> historicalEvents = getEvents();
    LOG.info("Returned events: {}", historicalEvents);
    return historicalEvents.get(new Random().nextInt(historicalEvents.size()));
  }

  private List<HistoricalEvent> getEvents() {
    InputStream inputStream = getClass().getClassLoader().getResourceAsStream("formatted-events.json");
    if (inputStream == null) {
      LOG.error("formatted-events.json not found in the classpath");
      return Collections.emptyList();
    }
    try {
      Map<Integer, List<HistoricalEvent>> eventsByYear = OBJECT_MAPPER.readValue(inputStream, new TypeReference<>() {
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
