
package com.unicity.sdk.api;

import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

public interface IAggregatorClient {
    CompletableFuture<Commitment> submitTransaction(Transaction transaction);
    CompletableFuture<InclusionProof> getInclusionProof(Commitment commitment);
}
