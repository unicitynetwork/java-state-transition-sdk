package com.unicity.sdk.api;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.jsonrpc.JsonRpcHttpTransport;
import com.unicity.sdk.transaction.InclusionProof;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

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