package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.TransactionData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for Commitment objects.
 */
public class CommitmentJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IPredicateFactory predicateFactory;
    
    public CommitmentJsonSerializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    /**
     * Serializes a Commitment object into a JSON representation.
     * @param commitment The commitment to serialize
     * @return JSON representation of the commitment
     */
    public static Object serialize(Commitment<?> commitment) {
        ObjectNode result = objectMapper.createObjectNode();

        result.set("transactionData", objectMapper.valueToTree(commitment.getTransactionData().toJSON()));
        result.set("authenticator", objectMapper.valueToTree(commitment.getAuthenticator().toJSON()));
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of commitment into a Commitment object.
     * @param tokenId The token ID context
     * @param tokenType The token type context
     * @param data The JSON data to deserialize
     * @return A promise that resolves to the deserialized Commitment object
     */
    public CompletableFuture<Commitment<TransactionData>> deserialize(
            TokenId tokenId, TokenType tokenType, JsonNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Deserialize RequestId
                String requestIdHex = data.get("requestId").asText();
                RequestId requestId = RequestId.fromJSON(requestIdHex);
                
                // Deserialize TransactionData with context
                JsonNode txDataNode = data.get("transactionData");
                TransactionData transactionData = 
                    TransactionDataJsonSerializer.deserialize(tokenId, tokenType, txDataNode).get();
                
                // Deserialize Authenticator
                JsonNode authNode = data.get("authenticator");
                // Convert JsonNode to Map for Authenticator.fromJSON
                Map<String, Object> authMap = new HashMap<>();
                authMap.put("algorithm", authNode.get("algorithm").asText());
                authMap.put("publicKey", authNode.get("publicKey").asText());
                authMap.put("signature", authNode.get("signature").asText());
                authMap.put("stateHash", authNode.get("stateHash").asText());
                Authenticator authenticator = Authenticator.fromJSON(authMap);
                
                return new Commitment<>(requestId, transactionData, authenticator);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize commitment", e);
            }
        });
    }
}