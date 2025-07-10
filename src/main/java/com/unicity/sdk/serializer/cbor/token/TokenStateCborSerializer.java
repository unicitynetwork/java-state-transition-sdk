package com.unicity.sdk.serializer.cbor.token;

import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A serializer for {@link TokenState} objects using CBOR encoding.
 * Handles serialization and deserialization of token states.
 */
public class TokenStateCborSerializer {
    private final IPredicateFactory predicateFactory;

    /**
     * Constructs a new TokenStateCborSerializer instance.
     *
     * @param predicateFactory A factory for creating predicates used in token state deserialization.
     */
    public TokenStateCborSerializer(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    /**
     * Serializes a TokenState object into a CBOR-encoded byte array.
     *
     * @param state The token state to serialize.
     * @return The CBOR-encoded representation of the token state.
     */
    public static byte[] serialize(TokenState state) {
        return CborEncoder.encodeArray(
            state.getUnlockPredicate().toCBOR(),
            state.getData() != null ? CborEncoder.encodeByteString(state.getData()) : CborEncoder.encodeNull()
        );
    }

    /**
     * Deserializes a CBOR-encoded byte array into a TokenState object.
     *
     * @param tokenId The ID of the token associated with the state.
     * @param tokenType The type of the token associated with the state.
     * @param bytes The CBOR-encoded data to deserialize.
     * @return A CompletableFuture that resolves to the deserialized TokenState object.
     */
    public CompletableFuture<TokenState> deserialize(TokenId tokenId, TokenType tokenType, byte[] bytes) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(bytes, 0);
            
            if (!(result.value instanceof List)) {
                return CompletableFuture.failedFuture(new RuntimeException("Expected array for TokenState"));
            }
            
            List<?> data = (List<?>) result.value;
            if (data.size() < 2) {
                return CompletableFuture.failedFuture(new RuntimeException("Invalid TokenState array size"));
            }
            
            // Deserialize unlock predicate
            return predicateFactory.create(tokenId, tokenType, (byte[]) data.get(0))
                .thenApply(unlockPredicate -> {
                    // Get optional data
                    byte[] stateData = null;
                    if (data.get(1) != null && data.get(1) instanceof byte[]) {
                        stateData = (byte[]) data.get(1);
                    }
                    
                    return TokenState.create(unlockPredicate, stateData);
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}