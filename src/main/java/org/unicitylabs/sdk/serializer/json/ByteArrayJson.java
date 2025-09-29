package org.unicitylabs.sdk.serializer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.unicitylabs.sdk.util.HexConverter;

public class ByteArrayJson {

  private ByteArrayJson() {
  }

  public static class Serializer extends StdSerializer<byte[]> {

    public Serializer() {
      super(byte[].class);
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

  public static class Deserializer extends StdDeserializer<byte[]> {
    public Deserializer() {
      super(byte[].class);
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

