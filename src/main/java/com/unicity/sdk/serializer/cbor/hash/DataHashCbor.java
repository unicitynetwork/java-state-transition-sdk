package com.unicity.sdk.serializer.cbor.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;

import java.io.IOException;

public class DataHashCbor {
    private DataHashCbor() {}

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
            byte[] value = p.getBinaryValue();
            if (value == null) {
                throw MismatchedInputException.from(p, DataHash.class, "Expected byte value");
            }

            try {
                return DataHash.fromImprint(value);
            } catch (Exception e) {
                throw MismatchedInputException.from(p, DataHash.class, "Expected byte value");
            }
        }
    }
}

