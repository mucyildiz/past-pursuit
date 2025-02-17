package org.pastpursuit.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.pastpursuit.ImmutableUser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Path("/auth/google")
public class GoogleAuthService {
  private static final String CLIENT_ID = "592653308145-kko8ckgfnfckcjl1d5lfa0h7kmghdr4l.apps.googleusercontent.com";
  private static final String REDIRECT_URI = "https://api.pastpursuit.io/api/auth/google/callback";
  public static final UserService userService = new UserService();

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
  public Response handleCallback(@QueryParam("code") String code) {
    GoogleIdToken.Payload payload;
    try {
      GoogleTokenResponse response = new GoogleAuthorizationCodeFlow(
        new NetHttpTransport(), null, CLIENT_ID, "secret", List.of("email"))
        .newTokenRequest(code)
        .execute();
      payload = response.parseIdToken().getPayload();
    } catch (IOException e) {
      return Response.serverError().build();
    }
    String email = payload.getEmail();
    Optional<ImmutableUser> user = userService.getByEmail(email);
    if (user.isEmpty()) {
      return Response.ok(email).build();
    } else {
      return Response.ok(user.get()).build();
    }
  }
}
