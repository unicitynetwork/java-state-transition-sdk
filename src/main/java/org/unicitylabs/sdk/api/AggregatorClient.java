package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.jsonrpc.JsonRpcHttpTransport;
import org.unicitylabs.sdk.transaction.InclusionProof;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

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
    return this.transport.request("submit_commitment", request, SubmitCommitmentResponse.class, this.apiKey);
  }

  public CompletableFuture<InclusionProof> getInclusionProof(RequestId requestId) {
    InclusionProofRequest request = new InclusionProofRequest(requestId);

    return this.transport.request("get_inclusion_proof", request, InclusionProof.class);
  }

  public CompletableFuture<Long> getBlockHeight() {
    return this.transport.request("get_block_height", Collections.emptyMap(),
            BlockHeightResponse.class)
        .thenApply(BlockHeightResponse::getBlockNumber);
  }
}