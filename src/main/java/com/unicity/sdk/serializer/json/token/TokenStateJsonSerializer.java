package com.unicity.sdk.serializer.json.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;

import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for TokenState objects.
 */
public class TokenStateJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final IPredicateFactory predicateFactory;
    
    public TokenStateJsonSerializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }
    
    /**
     * Serializes a TokenState object into a JSON representation.
     * @param state The token state to serialize
     * @return JSON representation of the token state
     */
    public static Object serialize(TokenState state) {
        ObjectNode result = objectMapper.createObjectNode();
        
//        result.set("unlockPredicate", objectMapper.valueToTree(state.getUnlockPredicate().toJSON()));
        result.put("data", HexConverter.encode(state.getData()));
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of token state into a TokenState object.
     * @param tokenId The token ID context for predicate creation
     * @param tokenType The token type context for predicate creation  
     * @param data The JSON data to deserialize
     * @return A promise that resolves to the deserialized TokenState object
     */
    public CompletableFuture<TokenState> deserialize(TokenId tokenId, TokenType tokenType, JsonNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Deserialize the unlock predicate with token context
                JsonNode predicateNode = data.get("unlockPredicate");
                IPredicate predicate = null;
                
                // Get state data
                String dataHex = data.get("data").asText();
                byte[] stateData = HexConverter.decode(dataHex);
                
                return TokenState.create(predicate, stateData);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize token state", e);
            }
        });
    }
}