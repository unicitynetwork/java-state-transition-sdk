package com.unicity.sdk.serializer.cbor.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.unicity.sdk.api.Authenticator;

import java.io.IOException;

public class AuthenticatorCborSerializer {
    private AuthenticatorCborSerializer() {}

    public static class Serializer extends JsonSerializer<Authenticator> {
        @Override
        public void serialize(Authenticator value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartArray(value, 4);
            gen.writeObject(value.getAlgorithm());
            gen.writeObject(value.getPublicKey());
            gen.writeObject(value.getSignature().encode());
            gen.writeObject(value.getStateHash());
            gen.writeEndArray();
        }
    }

    public static class Deserializer extends JsonDeserializer<Authenticator> {
        @Override
        public Authenticator deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            JsonNode node = ctx.readTree(p);
            if (node.isNull()) {
                return null;
            }

            return null;
        }
    }
}
