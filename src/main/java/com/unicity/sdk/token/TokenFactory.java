
package com.unicity.sdk.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.predicate.IPredicateFactory;
import com.unicity.sdk.serializer.token.TokenJsonDeserializer;

import java.util.concurrent.CompletableFuture;

public class TokenFactory {
    private final TokenJsonDeserializer tokenDeserializer;

    public TokenFactory(IPredicateFactory predicateFactory) {
        // TokenJsonDeserializer doesn't use predicateFactory currently
        this.tokenDeserializer = new TokenJsonDeserializer();
    }

    public CompletableFuture<Token<?>> create(Object data) {
        // Wrap synchronous result in CompletableFuture
        try {
            return CompletableFuture.completedFuture(tokenDeserializer.deserialize(data));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
