
package com.unicity.sdk.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.predicate.IPredicateFactory;

public class TokenFactory {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IPredicateFactory predicateFactory;

    public TokenFactory(IPredicateFactory predicateFactory) {
        this.predicateFactory = predicateFactory;
    }

    public Token create(Object data) {
        // This is a simplified example. In a real implementation, you would use the predicate factory to create the correct predicate.
        return objectMapper.convertValue(data, Token.class);
    }
}
