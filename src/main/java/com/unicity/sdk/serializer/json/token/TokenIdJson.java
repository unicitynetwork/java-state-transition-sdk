package com.unicity.sdk.serializer.json.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;

public class TokenIdJson {
    private TokenIdJson() {}


    public static class Serializer extends JsonSerializer<TokenId> {
        public Serializer() {}

        @Override
        public void serialize(TokenId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writePOJO(value.getBytes());
        }
    }

    public static class Deserializer extends JsonDeserializer<TokenId> {
        public Deserializer() {}

        @Override
        public TokenId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {

            try {
                return new TokenId(p.readValueAs(byte[].class));
            } catch (Exception e) {
                throw MismatchedInputException.from(p, TokenId.class, "Expected bytes");
            }
        }
    }
}

