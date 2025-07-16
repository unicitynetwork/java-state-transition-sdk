package com.unicity.sdk.serializer.transaction;

import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.Commitment;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * JSON deserializer for Commitment objects.
 */
public class CommitmentJsonDeserializer {
    private final IPredicateFactory predicateFactory;
    private final TransactionJsonDeserializer transactionDeserializer;

    public CommitmentJsonDeserializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
        this.transactionDeserializer = new TransactionJsonDeserializer();
    }

    public CompletableFuture<Commitment<?>> deserialize(TokenId tokenId, TokenType tokenType, Object data) {
        if (!(data instanceof Map)) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid commitment data"));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> commitmentData = (Map<String, Object>) data;

        try {
            // Get request ID
//            RequestId requestId = RequestId.fromJSON(String.valueOf(commitmentData.get("requestId")));
            
            // Deserialize transaction - it returns Transaction, not TransactionData
            // For commitments, we need the TransactionData, not the full Transaction
            // The commitment contains transaction data before it's submitted
            // We need to deserialize the transaction data directly
            Object txData = commitmentData.get("transactionData");
            
            // TODO: Implement proper TransactionData deserialization
            // For now, just return an error
            return CompletableFuture.failedFuture(new UnsupportedOperationException("TransactionData deserialization not implemented"));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}