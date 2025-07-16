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

/**
 * DataHashJson provides JSON serialization and deserialization for DataHash objects.
 * It uses Hex encoding to represent the hash imprint as a string in JSON.
 */
public class RequestIdJson {
    private RequestIdJson() {}

    /**
     * Serializer for DataHash objects.
     * Serializes the DataHash imprint as a Hex-encoded string.
     */
    public static class Serializer extends JsonSerializer<RequestId> {
        /**
         * Default constructor for the serializer.
         */
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

    /**
     * Deserializer for DataHash objects.
     * Expects a Hex-encoded string and converts it to a DataHash object.
     */
    public static class Deserializer extends JsonDeserializer<RequestId> {
        /**
         * Default constructor for the deserializer.
         */
        public Deserializer() {}

        @Override
        public RequestId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            return new RequestId(p.readValueAs(DataHash.class));
        }
    }
}

