package org.unicitylabs.sdk.api;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.jsonrpc.JsonRpcHttpTransport;

public class AggregatorClient implements IAggregatorClient {

  private final JsonRpcHttpTransport transport;

  public AggregatorClient(String url) {
    this.transport = new JsonRpcHttpTransport(url);
  }

  public CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator) {

    SubmitCommitmentRequest request = new SubmitCommitmentRequest(requestId, transactionHash,
        authenticator, false);
    return this.transport.request("submit_commitment", request, SubmitCommitmentResponse.class);
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