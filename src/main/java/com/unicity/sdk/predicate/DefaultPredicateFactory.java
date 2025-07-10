package com.unicity.sdk.predicate;

import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of IPredicateFactory.
 */
public class DefaultPredicateFactory implements IPredicateFactory {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public CompletableFuture<IPredicate> create(TokenId tokenId, TokenType tokenType, byte[] data) {
        try {
            // First decode CBOR to get the structure
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(data, 0);
            
            // Convert to JSON for PredicateFactory
            JsonNode jsonNode = objectMapper.valueToTree(result.value);
            
            // Use existing PredicateFactory
            return PredicateFactory.create(tokenId, tokenType, jsonNode);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}