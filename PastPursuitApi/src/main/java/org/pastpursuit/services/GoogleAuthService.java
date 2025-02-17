package org.pastpursuit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.ImmutableUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Path("/auth/google")
public class GoogleAuthService {
  private static final Logger LOG = LoggerFactory.getLogger(GoogleAuthService.class);
  private static final String CLIENT_ID = "592653308145-kko8ckgfnfckcjl1d5lfa0h7kmghdr4l.apps.googleusercontent.com";
  private static final String REDIRECT_URI = "https://api.pastpursuit.io/api/auth/google/callback";
  public static final UserService userService = new UserService();
  private String googleClientSecret;

  public GoogleAuthService() {
    googleClientSecret = System.getenv("GOOGLE_CLIENT_SECRET");
  }

  // implement google oauth here
  @GET
  public Response getAuthUrl() throws URISyntaxException {
    GoogleAuthorizationCodeRequestUrl url = new GoogleAuthorizationCodeRequestUrl(
      CLIENT_ID,
      REDIRECT_URI,
      List.of("email")
    );
    return Response.temporaryRedirect(url.toURI()).build();
  }

  @GET
  @Path("/callback")
  public Response handleCallback(@QueryParam("code") String code) throws JsonProcessingException {
    GoogleIdToken.Payload payload;
    try {
      GoogleTokenResponse response = new GoogleAuthorizationCodeFlow(
        new NetHttpTransport(),
        GsonFactory.getDefaultInstance(),
        CLIENT_ID,
        googleClientSecret,
        List.of("email")
      ).newTokenRequest(code)
        .setRedirectUri(REDIRECT_URI)
        .execute();
      payload = response.parseIdToken().getPayload();
    } catch (IOException e) {
      LOG.error("Error parsing google token", e);
      return Response.serverError().build();
    }
    String email = payload.getEmail();
    Optional<ImmutableUser> user = userService.getByEmail(email);
    if (user.isEmpty()) {
      LOG.info("User not found, creating new user with email: {}", email);
      String script = String.format("""
          <script>
              window.opener.postMessage(%s, "%s");
              window.close();
          </script>
          """,
        new ObjectMapper().writeValueAsString(email),
        "https://pastpursuit.io"
      );
      return Response.ok(script)
        .type(MediaType.TEXT_HTML)
        .build();
    } else {
      LOG.info("User {} logged in", user.get().getName());
      String script = String.format("""
          <script>
              window.opener.postMessage(%s, "%s");
              window.close();
          </script>
          """,
        new ObjectMapper().writeValueAsString(user.get()),
        "https://pastpursuit.io"
      );
      return Response.ok(script)
        .type(MediaType.TEXT_HTML)
        .build();
    }
  }
}
