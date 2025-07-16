
package com.unicity.sdk.api;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.transaction.InclusionProof;

import java.util.concurrent.CompletableFuture;

public interface IAggregatorClient {

  CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator);

  CompletableFuture<InclusionProof> getInclusionProof(RequestId requestId);

  CompletableFuture<Long> getBlockHeight();
}
