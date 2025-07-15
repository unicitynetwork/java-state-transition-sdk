package com.unicity.sdk.serializer.json.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;

import java.io.IOException;

public class DataHashJson {
    private DataHashJson() {}

    public static class Serializer extends JsonSerializer<DataHash> {
        @Override
        public void serialize(DataHash value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeString(HexConverter.encode(value.getImprint()));
        }
    }

    public static class Deserializer extends JsonDeserializer<DataHash> {
        @Override
        public DataHash deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            String value = p.getValueAsString();
            if (value == null) {
                ctx.handleUnexpectedToken(DataHash.class, p);
            }

            try {
                return DataHash.fromImprint(HexConverter.decode(value));
            } catch (Exception e) {
                ctx.reportInputMismatch(DataHash.class, "Expected string value");
                throw e;
            }
        }
    }
}

