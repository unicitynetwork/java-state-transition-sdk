package com.unicity.sdk.serializer.cbor.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;

import java.io.IOException;

public class DataHashCborSerializer {
    private DataHashCborSerializer() {}

    public static class Serializer extends JsonSerializer<DataHash> {
        @Override
        public void serialize(DataHash value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeBinary(value.getImprint());
        }
    }

    public static class Deserializer extends JsonDeserializer<DataHash> {
        @Override
        public DataHash deserialize(JsonParser p, DeserializationContext cxt) throws IOException {
            JsonNode value = cxt.readTree(p);
            if (value.isNull()) {
                return null;
            }

            Exception cause = null;
            if (value.isTextual()) {
                try {
                    return DataHash.fromImprint(HexConverter.decode(value.asText()));
                } catch (Exception e) {
                    cause = e;
                }
            }

            throw JsonMappingException.from(p, "Invalid DataHash JSON format", cause);
        }
    }
}

