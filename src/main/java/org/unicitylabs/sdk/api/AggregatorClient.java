package org.unicitylabs.sdk.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.jsonrpc.JsonRpcHttpTransport;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;

public class AggregatorClient implements IAggregatorClient {

  private final JsonRpcHttpTransport transport;
  private final String apiKey;

  public AggregatorClient(String url) {
    this(url, null);
  }

  public AggregatorClient(String url, String apiKey) {
    this.transport = new JsonRpcHttpTransport(url);
    this.apiKey = apiKey;
  }

  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator) {

    SubmitCommitmentRequest request = new SubmitCommitmentRequest(requestId, transactionHash,
        authenticator, false);
    Map<String, List<String>> headers = new LinkedHashMap<>();
    if (apiKey != null) {
        headers.put(AUTHORIZATION, Collections.singletonList("Bearer " + apiKey));
    }
    return this.transport.request("submit_commitment", request, SubmitCommitmentResponse.class, headers);
  }

  public CompletableFuture<InclusionProofResponse> getInclusionProof(RequestId requestId) {
    InclusionProofRequest request = new InclusionProofRequest(requestId);

    return this.transport.request("get_inclusion_proof", request, InclusionProofResponse.class);
  }

  public CompletableFuture<Long> getBlockHeight() {
    return this.transport.request("get_block_height", Collections.emptyMap(),
            BlockHeightResponse.class)
        .thenApply(BlockHeightResponse::getBlockNumber);
  }
}