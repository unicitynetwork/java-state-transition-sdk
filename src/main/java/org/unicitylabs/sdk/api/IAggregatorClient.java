
package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.transaction.InclusionProof;

import java.util.concurrent.CompletableFuture;

public interface IAggregatorClient {

  CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator);

  CompletableFuture<InclusionProof> getInclusionProof(RequestId requestId);

  CompletableFuture<Long> getBlockHeight();
}
