
package org.unicitylabs.sdk.api;

import java.util.concurrent.CompletableFuture;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * Aggregator client structure.
 */
public interface AggregatorClient {

  /**
   * Submit commitment.
   *
   * @param requestId       request id
   * @param transactionHash transaction hash
   * @param authenticator   authenticator
   * @return submit commitment response
   */
  CompletableFuture<SubmitCommitmentResponse> submitCommitment(
      RequestId requestId,
      DataHash transactionHash,
      Authenticator authenticator);

  /**
   * Get inclusion proof for request id.
   *
   * @param requestId request id
   * @return inclusion / non inclusion proof
   */
  CompletableFuture<InclusionProofResponse> getInclusionProof(RequestId requestId);

  /**
   * Get block height.
   *
   * @return block height
   */
  CompletableFuture<Long> getBlockHeight();
}
