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
        return transport.request("submitTransaction", transaction.toJSON())
                .thenApply(result -> objectMapper.convertValue(result, Commitment.class));
    }

    @Override
    public CompletableFuture<InclusionProof> getInclusionProof(Commitment commitment) {
        return transport.request("getInclusionProof", commitment.toJSON())
                .thenApply(result -> objectMapper.convertValue(result, InclusionProof.class));
    }
}