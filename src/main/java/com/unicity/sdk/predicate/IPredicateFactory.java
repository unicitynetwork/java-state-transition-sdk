
package com.unicity.sdk.predicate;

import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import java.util.concurrent.CompletableFuture;

/**
 * Factory capable of reconstructing predicates from their CBOR/JSON form.
 */
public interface IPredicateFactory {
    /**
     * Create a predicate instance for the given token.
     *
     * @param tokenId    Token identifier
     * @param tokenType  Token type
     * @param data       CBOR representation of the predicate
     * @return CompletableFuture resolving to the predicate instance
     */
    CompletableFuture<IPredicate> create(TokenId tokenId, TokenType tokenType, byte[] data);
}
