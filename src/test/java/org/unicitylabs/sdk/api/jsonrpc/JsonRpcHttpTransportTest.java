package org.unicitylabs.sdk.api.jsonrpc;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Transport hardening tests (H-03): redirects must not be followed, so authentication headers are
 * never replayed to a redirect target.
 */
public class JsonRpcHttpTransportTest {

  private MockWebServer server;

  @BeforeEach
  void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  public void doesNotFollowRedirects() throws Exception {
    // A redirect the client must NOT follow; its target would receive the replayed request.
    server.enqueue(new MockResponse()
            .setResponseCode(302)
            .setHeader("Location", "https://attacker.example.com/steal"));

    JsonRpcHttpTransport transport = new JsonRpcHttpTransport(server.url("/").toString());

    ExecutionException error = Assertions.assertThrows(
            ExecutionException.class,
            () -> transport.request("method", "params", String.class, Map.of())
                    .get(5, TimeUnit.SECONDS));
    Assertions.assertInstanceOf(JsonRpcNetworkException.class, error.getCause());
    Assertions.assertEquals(302, ((JsonRpcNetworkException) error.getCause()).getStatus());

    // Exactly one request was made: the redirect was surfaced, not followed.
    Assertions.assertEquals(1, server.getRequestCount());
    RecordedRequest request = server.takeRequest();
    Assertions.assertEquals("/", request.getPath());
  }
}
