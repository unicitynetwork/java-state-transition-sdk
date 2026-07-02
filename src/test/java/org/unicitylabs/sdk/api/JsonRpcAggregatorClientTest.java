package org.unicitylabs.sdk.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Construction-time transport-security checks for {@link JsonRpcAggregatorClient} (H-03),
 * mirroring the JS {@code AggregatorClientTest}.
 */
public class JsonRpcAggregatorClientTest {

  @Test
  public void rejectsApiKeyOverPlaintextHttp() {
    IllegalArgumentException exception = Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new JsonRpcAggregatorClient("http://example.com", "secret-key"));
    Assertions.assertEquals(
            "API key must not be sent over plaintext HTTP; use an https url.",
            exception.getMessage());
  }

  @Test
  public void allowsApiKeyOverHttps() {
    Assertions.assertDoesNotThrow(
            () -> new JsonRpcAggregatorClient("https://example.com", "secret-key"));
  }

  @Test
  public void allowsPlaintextHttpWhenNoApiKey() {
    Assertions.assertDoesNotThrow(() -> new JsonRpcAggregatorClient("http://example.com"));
  }

  @Test
  public void allowsApiKeyOverHttpWhenInsecureTransportAllowed() {
    Assertions.assertDoesNotThrow(
            () -> new JsonRpcAggregatorClient("http://example.com", "secret-key", true));
  }
}
