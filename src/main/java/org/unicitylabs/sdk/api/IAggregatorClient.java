
package org.unicitylabs.sdk.api;

import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.hash.DataHash;

public interface IAggregatorClient {

  CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator);

  CompletableFuture<InclusionProofResponse> getInclusionProof(RequestId requestId);

  CompletableFuture<Long> getBlockHeight();
}
