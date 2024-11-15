package org.pastpursuit;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

public class Resource {
    private static final Logger LOG = Logger.getLogger(Resource.class.getName());
    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig()
                .packages("org.pastpursuit.services") // Register package with REST endpoints
                .register(new org.glassfish.jersey.jackson.JacksonFeature()); // Enable Jackson for JSON

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws  InterruptedException {
        final HttpServer server = startServer();
        LOG.info("Jersey server started at " + BASE_URI);
        LOG.info("Press Ctrl+C to stop...");
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
        Thread.currentThread().join();
    }
}
