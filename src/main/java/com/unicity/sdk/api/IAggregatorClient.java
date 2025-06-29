
package com.unicity.sdk.api;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

public interface IAggregatorClient {
    CompletableFuture<SubmitCommitmentResponse> submitTransaction(
            RequestId requestId,
            DataHash transactionHash,
            Authenticator authenticator);
    
    CompletableFuture<InclusionProof> getInclusionProof(RequestId requestId);
    
    CompletableFuture<Long> getBlockHeight();
}
