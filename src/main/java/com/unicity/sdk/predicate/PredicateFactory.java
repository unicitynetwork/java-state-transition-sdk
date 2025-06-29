package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
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
     * This is used for predicates that are already calculated and don't need recalculation.
     * @param jsonNode JSON node containing predicate data
     * @return The appropriate predicate implementation
     */
    public static IPredicate fromJSON(JsonNode jsonNode) {
        try {
            String type = jsonNode.get("type").asText();
            
            // For predicates without token context, we need to extract the data differently
            JsonNode dataNode = jsonNode.get("data");
            if (dataNode == null) {
                dataNode = jsonNode;
            }
            
            switch (type) {
                case "MASKED":
                    return deserializeMaskedPredicateWithoutToken(dataNode);
                case "UNMASKED":
                    return deserializeUnmaskedPredicateWithoutToken(dataNode);
                case "BURN":
                    return new BurnPredicate(HashAlgorithm.SHA256);
                default:
                    throw new IllegalArgumentException("Unknown predicate type: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize predicate", e);
        }
    }
    
    private static IPredicate deserializeMaskedPredicateWithoutToken(JsonNode jsonNode) throws Exception {
        String publicKeyHex = jsonNode.get("publicKey").asText();
        String algorithm = jsonNode.get("algorithm").asText();
        int hashAlgorithmValue = jsonNode.get("hashAlgorithm").asInt();
        String nonceHex = jsonNode.get("nonce").asText();
        
        byte[] publicKey = HexConverter.decode(publicKeyHex);
        byte[] nonce = HexConverter.decode(nonceHex);
        HashAlgorithm hashAlgorithm = HashAlgorithm.fromValue(hashAlgorithmValue);
        
        // Create a simple predicate implementation for deserialization
        return new SimplePredicate(
            PredicateType.MASKED,
            publicKey,
            algorithm,
            hashAlgorithm,
            nonce
        );
    }
    
    private static IPredicate deserializeUnmaskedPredicateWithoutToken(JsonNode jsonNode) throws Exception {
        String publicKeyHex = jsonNode.get("publicKey").asText();
        String algorithm = jsonNode.get("algorithm").asText();
        String hashAlgorithmStr = jsonNode.get("hashAlgorithm").asText();
        String nonceHex = jsonNode.get("nonce").asText();
        
        byte[] publicKey = HexConverter.decode(publicKeyHex);
        byte[] nonce = HexConverter.decode(nonceHex);
        HashAlgorithm hashAlgorithm = HashAlgorithm.valueOf(hashAlgorithmStr);
        
        // Create a simple predicate implementation for deserialization
        return new SimplePredicate(
            PredicateType.UNMASKED,
            publicKey,
            algorithm,
            hashAlgorithm,
            nonce
        );
    }
    
    /**
     * Simple predicate implementation for deserialization without token context.
     */
    private static class SimplePredicate extends DefaultPredicate {
        public SimplePredicate(PredicateType type, byte[] publicKey, String algorithm, 
                             HashAlgorithm hashAlgorithm, byte[] nonce) {
            super(type, publicKey, algorithm, hashAlgorithm, nonce, 
                  new DataHash(new byte[32], HashAlgorithm.SHA256),  // dummy reference
                  new DataHash(new byte[32], HashAlgorithm.SHA256)); // dummy hash
        }
        
        @Override
        public Object toJSON() {
            // Simple JSON representation
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            root.put("type", getType().name());
            
            ObjectNode data = mapper.createObjectNode();
            data.put("publicKey", HexConverter.encode(getPublicKey()));
            data.put("algorithm", getAlgorithm());
            data.put("hashAlgorithm", getHashAlgorithm().getValue());
            data.put("nonce", HexConverter.encode(getNonce()));
            
            root.set("data", data);
            return root;
        }
        
        @Override
        public byte[] toCBOR() {
            // Simple CBOR encoding
            return CborEncoder.encodeArray(
                CborEncoder.encodeUnsignedInteger(getType().getValue()),
                CborEncoder.encodeArray(
                    CborEncoder.encodeByteString(getPublicKey()),
                    CborEncoder.encodeTextString(getAlgorithm()),
                    CborEncoder.encodeUnsignedInteger(getHashAlgorithm().getValue()),
                    CborEncoder.encodeByteString(getNonce())
                )
            );
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
            if (jsonNode == null) {
                return CompletableFuture.failedFuture(new IllegalArgumentException("Predicate JSON node is null"));
            }
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