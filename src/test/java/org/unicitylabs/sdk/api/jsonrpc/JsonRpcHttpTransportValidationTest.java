package org.unicitylabs.sdk.api.jsonrpc;

import okhttp3.mockwebserver.Dispatcher;
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
 * JSON-RPC response validation (L-02): id correlation, result/error exclusivity, version, and a
 * response body size limit.
 */
public class JsonRpcHttpTransportValidationTest {

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

  private static MockResponse json(String body) {
    return new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body);
  }

  private Throwable causeOf(JsonRpcHttpTransport transport) {
    ExecutionException error = Assertions.assertThrows(
            ExecutionException.class,
            () -> transport.request("method", "params", String.class, Map.of())
                    .get(5, TimeUnit.SECONDS));
    return error.getCause();
  }

  /**
   * Serve a body whose {@code %s} is filled with the actual request id, so checks that run after
   * id correlation can be targeted in isolation.
   */
  private Throwable requestWithEchoedId(String bodyTemplate) {
    server.setDispatcher(new Dispatcher() {
      @Override
      public MockResponse dispatch(RecordedRequest request) {
        String body = request.getBody().readUtf8();
        int start = body.indexOf("\"id\":\"") + 6;
        String id = body.substring(start, body.indexOf('"', start));
        return json(String.format(bodyTemplate, id));
      }
    });
    return causeOf(new JsonRpcHttpTransport(server.url("/").toString()));
  }

  @Test
  public void rejectsResponseWithMismatchedId() {
    server.enqueue(json(
            "{\"jsonrpc\":\"2.0\",\"result\":\"OK\","
                    + "\"id\":\"11111111-1111-1111-1111-111111111111\"}"));
    Throwable cause = causeOf(new JsonRpcHttpTransport(server.url("/").toString()));
    Assertions.assertTrue(cause.getMessage().contains("id mismatch"), cause.getMessage());
  }

  @Test
  public void rejectsResponseWithNeitherResultNorError() {
    Throwable cause = requestWithEchoedId("{\"jsonrpc\":\"2.0\",\"id\":\"%s\"}");
    Assertions.assertTrue(cause.getMessage().contains("exactly one"), cause.getMessage());
  }

  @Test
  public void rejectsResponseWithBothResultAndError() {
    Throwable cause = requestWithEchoedId(
            "{\"jsonrpc\":\"2.0\",\"result\":\"OK\",\"error\":{\"code\":1,\"message\":\"x\"},"
                    + "\"id\":\"%s\"}");
    Assertions.assertTrue(cause.getMessage().contains("exactly one"), cause.getMessage());
  }

  @Test
  public void rejectsUnsupportedVersion() {
    Throwable cause = requestWithEchoedId("{\"jsonrpc\":\"1.0\",\"result\":\"OK\",\"id\":\"%s\"}");
    Assertions.assertTrue(cause.getMessage().contains("Invalid JSON-RPC version"),
            cause.getMessage());
  }

  @Test
  public void rejectsOversizedResponse() {
    StringBuilder big = new StringBuilder("{\"jsonrpc\":\"2.0\",\"result\":\"");
    for (int i = 0; i < 5000; i++) {
      big.append('a');
    }
    big.append("\",\"id\":\"x\"}");

    server.enqueue(json(big.toString()));
    Throwable cause = causeOf(new JsonRpcHttpTransport(server.url("/").toString(), 1024));
    Assertions.assertTrue(cause.getMessage().contains("maximum allowed size"), cause.getMessage());
  }
}
