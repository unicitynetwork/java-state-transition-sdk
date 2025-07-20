package com.unicity.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;

public class RequestIdJson {
    private RequestIdJson() {}

    public static class Serializer extends JsonSerializer<RequestId> {
        public Serializer() {}

        @Override
        public void serialize(RequestId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writePOJO(value.getHash());
        }
    }

    public static class Deserializer extends JsonDeserializer<RequestId> {
        public Deserializer() {}

        @Override
        public RequestId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return new RequestId(p.readValueAs(DataHash.class));
        }
    }
}

