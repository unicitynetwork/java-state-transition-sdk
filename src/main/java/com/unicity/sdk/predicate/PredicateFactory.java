package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

import java.util.concurrent.CompletableFuture;

/**
 * Factory for creating predicates from JSON.
 */
public class PredicateFactory {
    
    /**
     * Create a predicate from JSON without token context.
     * This method is used when deserializing predicates that don't require token information.
     * @param jsonNode JSON node containing predicate data
     * @return The appropriate predicate implementation
     */
    public static IPredicate fromJSON(JsonNode jsonNode) throws Exception {
        String type = jsonNode.get("type").asText();
        
        switch (type) {
            case "MASKED":
                return MaskedPredicate.fromJSON(null, null, jsonNode).get();
            case "UNMASKED":
                return UnmaskedPredicate.fromJSON(null, null, jsonNode).get();
            case "BURN":
                return BurnPredicate.fromJSON(null, null, jsonNode).get();
            default:
                throw new IllegalArgumentException("Unknown predicate type: " + type);
        }
    }
    
    /**
     * Create a predicate from JSON with token context.
     * @param tokenId The token ID
     * @param tokenType The token type
     * @param jsonNode JSON node containing predicate data
     * @return The appropriate predicate implementation
     */
    public static CompletableFuture<IPredicate> create(TokenId tokenId, TokenType tokenType, JsonNode jsonNode) {
        try {
            String type = jsonNode.get("type").asText();
            
            switch (type) {
                case "MASKED":
                    return MaskedPredicate.fromJSON(tokenId, tokenType, jsonNode).thenApply(predicate -> (IPredicate) predicate);
                case "UNMASKED":
                    return UnmaskedPredicate.fromJSON(tokenId, tokenType, jsonNode).thenApply(predicate -> (IPredicate) predicate);
                case "BURN":
                    return BurnPredicate.fromJSON(tokenId, tokenType, jsonNode).thenApply(predicate -> (IPredicate) predicate);
                default:
                    return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown predicate type: " + type));
            }
        } catch (Exception e) {
            return CompletableFuture.failedFuture(new RuntimeException("Failed to deserialize predicate", e));
        }
    }
    
    private static IPredicate deserializeMaskedPredicate(TokenId tokenId, TokenType tokenType, JsonNode jsonNode) throws Exception {
        return MaskedPredicate.fromJSON(tokenId, tokenType, jsonNode).get();
    }
    
    private static IPredicate deserializeUnmaskedPredicate(TokenId tokenId, TokenType tokenType, JsonNode jsonNode) throws Exception {
        return UnmaskedPredicate.fromJSON(tokenId, tokenType, jsonNode).get();
    }
}