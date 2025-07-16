package com.unicity.sdk.serializer.cbor.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenType;

import java.io.IOException;

public class TokenTypeCbor {
    private TokenTypeCbor() {}

    public static class Serializer extends JsonSerializer<TokenType> {
        @Override
        public void serialize(TokenType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeBinary(value.getBytes());
        }
    }

    public static class Deserializer extends JsonDeserializer<TokenType> {
        @Override
        public TokenType deserialize(JsonParser p, DeserializationContext cxt) throws IOException {
            byte[] value = p.getBinaryValue();
            if (value == null) {
                throw MismatchedInputException.from(p, DataHash.class, "Expected byte value");
            }

            try {
                return new TokenType(value);
            } catch (Exception e) {
                throw MismatchedInputException.from(p, DataHash.class, "Expected byte value");
            }
        }
    }
}
