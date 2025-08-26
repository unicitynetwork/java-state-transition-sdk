package com.unicity.sdk.serializer.json.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.util.HexConverter;

import java.io.IOException;

/**
 * DataHashJson provides JSON serialization and deserialization for DataHash objects. It uses Hex
 * encoding to represent the hash imprint as a string in JSON.
 */
public class DataHashJson {

  private DataHashJson() {
  }

  /**
   * Serializer for DataHash objects. Serializes the DataHash imprint as a Hex-encoded string.
   */
  public static class Serializer extends JsonSerializer<DataHash> {

    /**
     * Default constructor for the serializer.
     */
    public Serializer() {
    }

    @Override
    public void serialize(DataHash value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeString(HexConverter.encode(value.getImprint()));
    }
  }

  /**
   * Deserializer for DataHash objects. Expects a Hex-encoded string and converts it to a DataHash
   * object.
   */
  public static class Deserializer extends JsonDeserializer<DataHash> {

    /**
     * Default constructor for the deserializer.
     */
    public Deserializer() {
    }

    @Override
    public DataHash deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(p, DataHash.class,
            "Expected string value");
      }

      try {
        return DataHash.fromImprint(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, DataHash.class, "Expected bytes");
      }
    }
  }
}

