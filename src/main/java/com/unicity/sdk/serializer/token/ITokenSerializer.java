package com.unicity.sdk.serializer.token;

import com.unicity.sdk.token.Token;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for token serializers capable of deserializing token transaction data.
 */
public interface ITokenSerializer {
    /**
     * Deserializes data into a Token.
     * @param data The data to deserialize.
     * @return A CompletableFuture resolving to the deserialized Token.
     */
    CompletableFuture<Token> deserialize(byte[] data);
}