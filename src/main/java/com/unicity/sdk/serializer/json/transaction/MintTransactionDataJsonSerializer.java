package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.predicate.PredicateFactory;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;

import java.util.concurrent.CompletableFuture;

/**
 * JSON serializer for MintTransactionData objects.
 */
public class MintTransactionDataJsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Serializes a MintTransactionData object into a JSON representation.
     * @param data The mint transaction data to serialize
     * @return JSON representation of the mint transaction data
     */
    public static Object serialize(MintTransactionData<ISerializable> data) {
        ObjectNode result = objectMapper.createObjectNode();
        
        result.put("tokenId", data.getTokenId().toJSON());
        result.put("tokenType", data.getTokenType().toJSON());
        result.set("unlockPredicate", objectMapper.valueToTree(data.getPredicate().toJSON()));
        
        // Handle token data
        if (data.getTokenData() != null) {
            result.set("tokenData", objectMapper.valueToTree(data.getTokenData().toJSON()));
        } else {
            result.putNull("tokenData");
        }
        
        // Handle coin data
        if (data.getCoinData() != null) {
            result.set("coinData", objectMapper.valueToTree(data.getCoinData().toJSON()));
        } else {
            result.putNull("coinData");
        }
        
        result.put("salt", HexConverter.encode(data.getSalt()));
        result.put("recipient", data.getRecipient().toString());
        
        // Handle data hash
        if (data.getDataHash() != null) {

        } else {
            result.putNull("dataHash");
        }
        
        return result;
    }
    
    /**
     * Deserializes a JSON representation of mint transaction data into a MintTransactionData object.
     * @param tokenId The token ID context
     * @param tokenType The token type context
     * @param data The JSON data to deserialize
     * @return A promise that resolves to the deserialized MintTransactionData object
     */
    public static CompletableFuture<MintTransactionData<ISerializable>> deserialize(
            TokenId tokenId, TokenType tokenType, JsonNode data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Deserialize unlock predicate with context
                JsonNode predicateNode = data.get("unlockPredicate");
                IPredicate unlockPredicate = PredicateFactory.create(tokenId, tokenType, predicateNode).get();
                
                // Deserialize token data
                ISerializable tokenData = null;
                JsonNode tokenDataNode = data.get("tokenData");
                if (tokenDataNode != null && !tokenDataNode.isNull()) {
                    if (tokenDataNode.isTextual()) {
                        // Direct hex string (e.g., from TestTokenData)
                        byte[] dataBytes = HexConverter.decode(tokenDataNode.asText());
                        tokenData = new SimpleTokenData(dataBytes);
                    } else if (tokenDataNode.isObject() && tokenDataNode.has("data")) {
                        // Object with a "data" field
                        String tokenDataHex = tokenDataNode.get("data").asText();
                        byte[] dataBytes = HexConverter.decode(tokenDataHex);
                        tokenData = new SimpleTokenData(dataBytes);
                    }
                }
                
                // Deserialize coin data
                TokenCoinData coinData = null;
                JsonNode coinDataNode = data.get("coinData");
                if (coinDataNode != null && !coinDataNode.isNull()) {
                    coinData = TokenCoinData.fromJSON(coinDataNode);
                }
                
                // Get other fields
                String saltHex = data.get("salt").asText();
                byte[] salt = HexConverter.decode(saltHex);
                
                String recipient = data.get("recipient").asText();
                
                JsonNode dataHashNode = data.get("dataHash");
                DataHash dataHash = null;
                if (dataHashNode != null && !dataHashNode.isNull()) {
                    dataHash = DataHash.fromJSON(dataHashNode.asText());
                }
                
                return new MintTransactionData<>(
                    tokenId,
                    tokenType,
                    unlockPredicate,
                    tokenData,
                    coinData,
                    dataHash,
                    salt
                );
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize mint transaction data", e);
            }
        });
    }
    
    /**
     * Simple wrapper for token data during deserialization.
     */
    private static class SimpleTokenData implements ISerializable {
        private final byte[] data;
        
        public SimpleTokenData(byte[] data) {
            this.data = java.util.Arrays.copyOf(data, data.length);
        }
        
        public byte[] getData() {
            return java.util.Arrays.copyOf(data, data.length);
        }
        
        @Override
        public Object toJSON() {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("data", HexConverter.encode(data));
            return node;
        }
        
        @Override
        public byte[] toCBOR() {
            return com.unicity.sdk.shared.cbor.CborEncoder.encodeByteString(data);
        }
    }
}