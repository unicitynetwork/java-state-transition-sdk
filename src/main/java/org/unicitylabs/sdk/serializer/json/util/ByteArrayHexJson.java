package org.unicitylabs.sdk.serializer.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.util.HexConverter;
import java.io.IOException;

public class ByteArrayHexJson {

  private ByteArrayHexJson() {
  }

  public static class Serializer extends JsonSerializer<byte[]> {

    public Serializer() {
    }

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeString(HexConverter.encode(value));
    }
  }

  public static class Deserializer extends JsonDeserializer<byte[]> {

    public Deserializer() {
    }

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(p, byte[].class,
            "Expected hex string value");
      }

      try {
        return HexConverter.decode(p.readValueAs(String.class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, byte[].class, "Expected hex string value");
      }
    }
  }
}

