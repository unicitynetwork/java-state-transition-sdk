
package com.unicity.sdk.serializer.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicity.sdk.token.Token;

public class TokenJsonDeserializer implements ITokenDeserializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Token deserialize(Object data) {
        return objectMapper.convertValue(data, Token.class);
    }
}
