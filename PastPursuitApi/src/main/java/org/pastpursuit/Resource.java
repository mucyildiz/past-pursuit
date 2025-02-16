package org.pastpursuit;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.pastpursuit.services.SocketsService;

import java.net.URI;
import java.util.logging.Logger;

public class Resource {
  private static final Logger LOG = Logger.getLogger(Resource.class.getName());
  // Run the HTTP server on port 8080 (both REST API and WebSocket will share this port)
  public static final String BASE_URI = "http://0.0.0.0:8080/api";

  public static void main(String[] args) {
    final ResourceConfig rc = new ResourceConfig().packages("org.pastpursuit.services").register(new org.glassfish.jersey.jackson.JacksonFeature());
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc, false);

    WebSocketAddOn webSocketAddOn = new WebSocketAddOn();
    for (NetworkListener l : server.getListeners()) {
      l.registerAddOn(webSocketAddOn);
    }

    // Clients will connect to ws://<host>:8080/ws
    WebSocketEngine.getEngine().register("", "/", new SocketsService());

    LOG.info("Jersey (REST) server started at " + BASE_URI);
    LOG.info("WebSocket server started at ws://0.0.0.0:8080/ws");
    LOG.info("Press Ctrl+C to stop...");
    try {
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
      LOG.severe(e.getMessage());
      server.shutdownNow();
    }
  }
}
