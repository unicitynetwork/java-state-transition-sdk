package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.serializer.json.transaction.TransactionDataJsonSerializer;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;

import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for Transaction objects.
 */
public class TransactionJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IPredicateFactory predicateFactory;
    
    public TransactionJsonSerializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    /**
     * Serializes a Transaction object into a JSON representation.
     * @param transaction The transaction to serialize
     * @return JSON representation of the transaction
     */
    public static Object serialize(Transaction<TransactionData> transaction) {
        ObjectNode result = objectMapper.createObjectNode();
        
        result.set("data", objectMapper.valueToTree(TransactionDataJsonSerializer.serialize(transaction.getData())));
        result.set("inclusionProof", objectMapper.valueToTree(transaction.getInclusionProof().toJSON()));
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of transaction into a Transaction object.
     * @param tokenId The token ID context
     * @param tokenType The token type context
     * @param data The JSON data to deserialize
     * @return A promise that resolves to the deserialized Transaction object
     */
    public CompletableFuture<Transaction<TransactionData>> deserialize(
            TokenId tokenId, TokenType tokenType, JsonNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Deserialize transaction data with context
                JsonNode txDataNode = data.get("data");
                TransactionData transactionData = 
                    TransactionDataJsonSerializer.deserialize(tokenId, tokenType, txDataNode).get();
                
                // Deserialize inclusion proof
                JsonNode proofNode = data.get("inclusionProof");
                InclusionProof inclusionProof = InclusionProof.fromJSON(proofNode);
                
                return new Transaction<>(transactionData, inclusionProof);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize transaction", e);
            }
        });
    }
}