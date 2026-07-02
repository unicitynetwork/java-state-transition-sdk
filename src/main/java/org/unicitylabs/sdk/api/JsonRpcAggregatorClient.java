package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.api.jsonrpc.JsonRpcHttpTransport;
import org.unicitylabs.sdk.util.HexConverter;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

/**
 * Default aggregator client.
 */
public class JsonRpcAggregatorClient implements AggregatorClient {
  private static final String STATE_ID_HEADER = "X-State-ID";

  private final JsonRpcHttpTransport transport;
  private final String apiKey;

  /**
   * Create aggregator client for destination url.
   *
   * @param url destination url
   */
  public JsonRpcAggregatorClient(String url) {
    this(url, null);
  }


  /**
   * Create aggregator client for destination url with api key. When an api key is supplied the
   * url must be {@code https}, so the key is never sent over plaintext.
   *
   * @param url    destination url
   * @param apiKey api key
   *
   * @throws IllegalArgumentException if an api key is supplied for a non-https url
   */
  public JsonRpcAggregatorClient(String url, String apiKey) {
    this(url, apiKey, false);
  }

  /**
   * Create aggregator client for destination url with api key.
   *
   * @param url    destination url
   * @param apiKey api key
   * @param allowInsecureTransport when {@code true}, permit sending the api key over a non-https
   *     url (intended for local development and testing only)
   *
   * @throws IllegalArgumentException if an api key is supplied for a non-https url and
   *     {@code allowInsecureTransport} is {@code false}
   */
  public JsonRpcAggregatorClient(String url, String apiKey, boolean allowInsecureTransport) {
    Objects.requireNonNull(url, "url cannot be null");

    if (apiKey != null && !allowInsecureTransport && !JsonRpcAggregatorClient.isHttps(url)) {
      throw new IllegalArgumentException(
              "API key must not be sent over plaintext HTTP; use an https url.");
    }

    this.transport = new JsonRpcHttpTransport(url);
    this.apiKey = apiKey;
  }

  private static boolean isHttps(String url) {
    String scheme = URI.create(url).getScheme();
    return scheme != null && scheme.toLowerCase(Locale.ROOT).equals("https");
  }

  /**
   * Submit a certification request for a transaction state transition.
   *
   * @param certificationData certification payload
   *
   * @return asynchronous certification response
   */
  @Override
  public CompletableFuture<CertificationResponse> submitCertificationRequest(
          CertificationData certificationData
  ) {
    CertificationRequest request = CertificationRequest.create(
            Objects.requireNonNull(certificationData, "certificationData cannot be null"));

    Map<String, List<String>> headers = new HashMap<>();
    headers.put(STATE_ID_HEADER, List.of(HexConverter.encode(request.getStateId().getData())));
    if (this.apiKey != null) {
      headers.put(AUTHORIZATION, List.of(String.format("Bearer %s", this.apiKey)));
    }

    return this.transport.request(
            "certification_request",
            HexConverter.encode(request.toCbor()),
            CertificationResponse.class,
            headers
    );
  }

  /**
   * Get inclusion proof for state id.
   *
   * @param stateId state id
   * @return inclusion / non inclusion proof
   */
  @Override
  public CompletableFuture<InclusionProofResponse> getInclusionProof(StateId stateId) {
    InclusionProofRequest request = new InclusionProofRequest(
            Objects.requireNonNull(stateId, "stateId cannot be null"));

    return this.transport
            .request("get_inclusion_proof.v2", request, String.class)
            .thenApply(response -> InclusionProofResponse.fromCbor(HexConverter.decode(response)));
  }

  /**
   * Get the latest block number.
   *
   * @return latest block number
   */
  @Override
  public CompletableFuture<Long> getLatestBlockNumber() {
    return this.transport.request("get_block_height", Map.of(), BlockHeightResponse.class)
            .thenApply(BlockHeightResponse::getBlockNumber);
  }
}
