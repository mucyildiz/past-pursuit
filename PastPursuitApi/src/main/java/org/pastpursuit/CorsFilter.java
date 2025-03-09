package org.pastpursuit;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext,
    ContainerResponseContext responseContext) throws IOException {
    boolean useLocalOrigin = System.getenv("USE_LOCAL_ORIGIN") != null;
    String origin = useLocalOrigin ?
                    "http://localhost:5173" :
                    "https://pastpursuit.io";

    responseContext.getHeaders().add(
      "Access-Control-Allow-Origin", origin);
    responseContext.getHeaders().add(
      "Access-Control-Allow-Credentials", "true");
    responseContext.getHeaders()
      .add("Access-Control-Allow-Private-Network", "true");
    responseContext.getHeaders().add(
      "Access-Control-Allow-Headers",
      "Content-Type, Authorization");
    responseContext.getHeaders().add(
      "Access-Control-Allow-Methods",
      "GET, POST, PUT, DELETE, OPTIONS, HEAD");
  }
}