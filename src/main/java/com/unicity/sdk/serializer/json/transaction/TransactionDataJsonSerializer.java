package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.predicate.PredicateFactory;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.TransactionData;

import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for TransactionData objects.
 */
public class TransactionDataJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Serializes a TransactionData object into a JSON representation.
     * @param data The transaction data to serialize
     * @return JSON representation of the transaction data
     */
    public static Object serialize(TransactionData data) {
        ObjectNode result = objectMapper.createObjectNode();
        
        result.set("sourceState", objectMapper.valueToTree(data.getSourceState().toJSON()));
        result.put("recipient", data.getRecipient());
        result.put("salt", HexConverter.encode(data.getSalt()));
        
        // Handle data hash
        if (data.getDataHash() != null) {

        } else {
            result.putNull("data");
        }
        
        result.put("message", HexConverter.encode(data.getMessage()));
        
        // TODO: Add nametag tokens when implemented
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of transaction data into a TransactionData object.
     * @param tokenId The token ID context
     * @param tokenType The token type context
     * @param jsonNode The JSON data to deserialize
     * @return A promise that resolves to the deserialized TransactionData object
     */
    public static CompletableFuture<TransactionData> deserialize(
            TokenId tokenId, TokenType tokenType, JsonNode jsonNode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Deserialize source state with context
                JsonNode sourceStateNode = jsonNode.get("sourceState");
                TokenState sourceState = deserializeTokenState(sourceStateNode, tokenId, tokenType);
                
                // Get recipient
                String recipient = jsonNode.get("recipient").asText();
                
                // Get salt
                String saltHex = jsonNode.get("salt").asText();
                byte[] salt = HexConverter.decode(saltHex);
                
                // Get data hash
                JsonNode dataNode = jsonNode.get("data");
                DataHash data = null;
                if (dataNode != null && !dataNode.isNull()) {
                    data = DataHash.fromJSON(dataNode.asText());
                }
                
                // Get message
                String messageHex = jsonNode.get("message").asText();
                byte[] message = HexConverter.decode(messageHex);
                
                // TODO: Handle nametag tokens when implemented
                
                return TransactionData.create(sourceState, recipient, salt, data, message).get();
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize transaction data", e);
            }
        });
    }
    
    private static TokenState deserializeTokenState(JsonNode stateNode, TokenId tokenId, TokenType tokenType) throws Exception {
        // Get state data
        String dataHex = stateNode.get("data").asText();
        byte[] data = HexConverter.decode(dataHex);
        
        // Deserialize predicate with token context
        JsonNode predicateNode = stateNode.get("unlockPredicate");
        if (predicateNode == null) {
            throw new IllegalArgumentException("Missing 'unlockPredicate' in token state JSON");
        }
        var predicate = PredicateFactory.create(tokenId, tokenType, predicateNode).get();
        
        return TokenState.create(predicate, data);
    }
}