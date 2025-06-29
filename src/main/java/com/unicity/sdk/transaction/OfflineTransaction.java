package com.unicity.sdk.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Represents a transaction with its commitment for offline processing.
 */
public class OfflineTransaction implements ISerializable {
    private final OfflineCommitment commitment;
    private final Token<Transaction<MintTransactionData<?>>> token;

    /**
     * @param commitment  The commitment for the transaction
     * @param token      The token being transferred
     */
    public OfflineTransaction(OfflineCommitment commitment, Token<Transaction<MintTransactionData<?>>> token) {
        if (commitment == null) {
            throw new IllegalArgumentException("Commitment cannot be null");
        }
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        this.commitment = commitment;
        this.token = token;
    }

    /**
     * Create OfflineTransaction from JSON data.
     * @param jsonString JSON string
     */
    public static CompletableFuture<OfflineTransaction> fromJSON(String jsonString) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(jsonString);
                
                if (!rootNode.has("commitment") || !rootNode.has("token")) {
                    throw new IllegalArgumentException("Invalid offline transaction JSON format");
                }
                
                JsonNode commitmentNode = rootNode.get("commitment");
                JsonNode tokenNode = rootNode.get("token");
                
                // Deserialize the commitment
                OfflineCommitment commitment = deserializeCommitment(commitmentNode);
                
                // Deserialize the token
                Token<Transaction<MintTransactionData<?>>> token = deserializeToken(tokenNode);
                
                return new OfflineTransaction(commitment, token);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize OfflineTransaction", e);
            }
        });
    }
    
    private static OfflineCommitment deserializeCommitment(JsonNode commitmentNode) throws Exception {
        // Deserialize RequestId
        String requestIdHex = commitmentNode.get("requestId").asText();
        RequestId requestId = RequestId.fromJSON(requestIdHex);
        
        // Deserialize TransactionData
        JsonNode txDataNode = commitmentNode.get("transactionData");
        TransactionData transactionData = TransactionData.fromJSON(txDataNode);
        
        // Deserialize Authenticator
        JsonNode authNode = commitmentNode.get("authenticator");
        Authenticator authenticator = Authenticator.fromJSON(authNode);
        
        return new OfflineCommitment(requestId, transactionData, authenticator);
    }
    
    private static Token<Transaction<MintTransactionData<?>>> deserializeToken(JsonNode tokenNode) throws Exception {
        return Token.fromJSON(tokenNode).get();
    }

    public OfflineCommitment getCommitment() {
        return commitment;
    }

    public Token<Transaction<MintTransactionData<?>>> getToken() {
        return token;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.set("commitment", mapper.valueToTree(commitment.toJSON()));
        root.set("token", mapper.valueToTree(token.toJSON()));
        
        return root;
    }

    /**
     * Serialize to JSON string.
     * @return JSON string representation
     */
    public String toJSONString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(toJSON());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            CborEncoder.encodeArray(
                commitment.getRequestId().toCBOR(),
                commitment.getTransactionData().toCBOR(),
                commitment.getAuthenticator().toCBOR()
            ),
            token.toCBOR()
        );
    }
}
