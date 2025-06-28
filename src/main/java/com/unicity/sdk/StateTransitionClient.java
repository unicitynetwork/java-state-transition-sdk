
package com.unicity.sdk;

import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

public class StateTransitionClient {
    private final AggregatorClient aggregatorClient;

    public StateTransitionClient(AggregatorClient aggregatorClient) {
        this.aggregatorClient = aggregatorClient;
    }

    public CompletableFuture<Commitment> submitTransaction(Transaction transaction) {
        return aggregatorClient.submitTransaction(transaction);
    }

    public CompletableFuture<InclusionProof> getInclusionProof(Commitment commitment) {
        return aggregatorClient.getInclusionProof(commitment);
    }
    
    public AggregatorClient getAggregatorClient() {
        return aggregatorClient;
    }
}
