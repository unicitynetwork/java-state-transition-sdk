package com.unicity.sdk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.shared.jsonrpc.JsonRpcHttpTransport;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

public class AggregatorClient implements IAggregatorClient {
    private final JsonRpcHttpTransport transport;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AggregatorClient(String url) {
        this.transport = new JsonRpcHttpTransport(url);
    }

    @Override
    public CompletableFuture<Commitment> submitTransaction(Transaction transaction) {
        return transport.send("submitTransaction", transaction.toJSON())
                .thenApply(response -> {
                    if (response.getError() != null) {
                        throw new RuntimeException(response.getError().getMessage());
                    }
                    return objectMapper.convertValue(response.getResult(), Commitment.class);
                });
    }

    @Override
    public CompletableFuture<InclusionProof> getInclusionProof(Commitment commitment) {
        return transport.send("getInclusionProof", commitment.toJSON())
                .thenApply(response -> {
                    if (response.getError() != null) {
                        throw new RuntimeException(response.getError().getMessage());
                    }
                    return objectMapper.convertValue(response.getResult(), InclusionProof.class);
                });
    }
}