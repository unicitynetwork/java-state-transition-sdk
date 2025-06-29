package com.unicity.sdk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.jsonrpc.JsonRpcHttpTransport;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AggregatorClient implements IAggregatorClient {
    private final JsonRpcHttpTransport transport;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AggregatorClient(String url) {
        this.transport = new JsonRpcHttpTransport(url);
    }

    public CompletableFuture<SubmitCommitmentResponse> submitTransaction(
            RequestId requestId,
            DataHash transactionHash,
            Authenticator authenticator) {
        Map<String, Object> params = new HashMap<>();
        params.put("requestId", requestId.toJSON());
        params.put("transactionHash", transactionHash.toJSON());
        params.put("authenticator", authenticator.toJSON());
        params.put("receipt", false);
        
        return transport.request("submit_commitment", params)
                .thenApply(result -> {
                    // TODO: Properly deserialize response
                    return new SubmitCommitmentResponse(SubmitCommitmentStatus.SUCCESS);
                });
    }

    public CompletableFuture<InclusionProof> getInclusionProof(RequestId requestId) {
        Map<String, Object> params = new HashMap<>();
        params.put("requestId", requestId.toJSON());
        
        return transport.request("get_inclusion_proof", params)
                .thenApply(result -> {
                    // TODO: Properly deserialize InclusionProof
                    return objectMapper.convertValue(result, InclusionProof.class);
                });
    }

    public CompletableFuture<Long> getBlockHeight() {
        return transport.request("get_block_height", new HashMap<>())
                .thenApply(result -> {
                    if (result instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) result;
                        Object blockNumber = map.get("blockNumber");
                        if (blockNumber instanceof String) {
                            return Long.parseLong((String) blockNumber);
                        } else if (blockNumber instanceof Number) {
                            return ((Number) blockNumber).longValue();
                        }
                    }
                    return 0L;
                });
    }
}