package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.serializer.json.TokenJsonSerializer;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;

import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for MintTransaction objects.
 */
public class MintTransactionJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final TokenJsonSerializer tokenSerializer;
    
    public MintTransactionJsonSerializer(TokenJsonSerializer tokenSerializer) {
        this.tokenSerializer = tokenSerializer;
    }
    
    /**
     * Serializes a MintTransaction object into a JSON representation.
     * @param transaction The mint transaction to serialize
     * @return JSON representation of the mint transaction
     */
    public static Object serialize(Transaction<MintTransactionData<ISerializable>> transaction) {
        ObjectNode result = objectMapper.createObjectNode();
        
        result.set("data", objectMapper.valueToTree(MintTransactionDataJsonSerializer.serialize(transaction.getData())));
        result.set("inclusionProof", objectMapper.valueToTree(transaction.getInclusionProof().toJSON()));
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of mint transaction into a Transaction object.
     * @param data The JSON data to deserialize
     * @return A promise that resolves to the deserialized Transaction object
     */
    public CompletableFuture<Transaction<MintTransactionData<ISerializable>>> deserialize(JsonNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First extract tokenId and tokenType from mint data to get context
                JsonNode mintDataNode = data.get("data");
                String tokenIdHex = mintDataNode.get("tokenId").asText();
                String tokenTypeHex = mintDataNode.get("tokenType").asText();
                
                TokenId tokenId = TokenId.fromHex(tokenIdHex);
                TokenType tokenType = TokenType.fromHex(tokenTypeHex);
                
                // Deserialize mint transaction data with context
                MintTransactionData<ISerializable> mintData = 
                    MintTransactionDataJsonSerializer.deserialize(tokenId, tokenType, mintDataNode).get();
                
                // Deserialize inclusion proof
                JsonNode proofNode = data.get("inclusionProof");
                InclusionProof inclusionProof = InclusionProof.fromJSON(proofNode);
                
                return new Transaction<>(mintData, inclusionProof);
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize mint transaction", e);
            }
        });
    }
}