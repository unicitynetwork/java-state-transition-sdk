
package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;

/**
 * Factory capable of reconstructing predicates from their CBOR/JSON form.
 */
public interface IPredicateFactory {
    /**
     * Create a predicate instance for the given token.
     *
     * @param tokenId    Token identifier
     * @param tokenType  Token type
     * @param data       Predicate data
     * @return CompletableFuture resolving to the predicate instance
     */
    Predicate create(TokenId tokenId, TokenType tokenType, Object data);
}
