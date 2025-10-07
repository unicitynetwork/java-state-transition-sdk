package org.unicitylabs.sdk.api;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.jsonrpc.JsonRpcHttpTransport;

/**
 * Default aggregator client.
 */
public class DefaultAggregatorClient implements AggregatorClient {

  private final JsonRpcHttpTransport transport;
  private final String apiKey;

  /**
   * Create aggregator client for destination url.
   *
   * @param url destination url
   */
  public DefaultAggregatorClient(String url) {
    this(url, null);
  }


  /**
   * Create aggregator client for destination url with api key.
   *
   * @param url    destination url
   * @param apiKey api key
   *
   */
  public DefaultAggregatorClient(String url, String apiKey) {
    this.transport = new JsonRpcHttpTransport(url);
    this.apiKey = apiKey;
  }

  /**
   * Submit commitment.
   *
   * @param requestId       request id
   * @param transactionHash transaction hash
   * @param authenticator   authenticator
   * @return submit commitment response
   */
  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator
  ) {
    SubmitCommitmentRequest request = new SubmitCommitmentRequest(
        requestId,
        transactionHash,
        authenticator,
        false
    );

    Map<String, List<String>> headers = this.apiKey == null
        ? Map.of()
        : Map.of(AUTHORIZATION, List.of(String.format("Bearer %s", this.apiKey)));

    return this.transport.request(
        "submit_commitment",
        request,
        SubmitCommitmentResponse.class,
        headers
    );
  }

  /**
   * Get inclusion proof for request id.
   *
   * @param requestId request id
   * @return inclusion / non inclusion proof
   */
  public CompletableFuture<InclusionProofResponse> getInclusionProof(RequestId requestId) {
    InclusionProofRequest request = new InclusionProofRequest(requestId);

    return this.transport.request("get_inclusion_proof", request, InclusionProofResponse.class);
  }

  /**
   * Get block height.
   *
   * @return block height
   */
  public CompletableFuture<Long> getBlockHeight() {
    return this.transport.request("get_block_height", Collections.emptyMap(),
            BlockHeightResponse.class)
        .thenApply(BlockHeightResponse::getBlockNumber);
  }
}