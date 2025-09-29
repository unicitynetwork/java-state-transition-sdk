package org.unicitylabs.sdk.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class DataHashJson {

  private DataHashJson() {
  }

  public static class Serializer extends StdSerializer<DataHash> {

    public Serializer() {
      super(DataHash.class);
    }

    @Override
    public void serialize(DataHash value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeObject(value.getImprint());
    }
  }

  public static class Deserializer extends StdDeserializer<DataHash> {

    public Deserializer() {
      super(DataHash.class);
    }

    @Override
    public DataHash deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            DataHash.class,
            "Expected string value"
        );
      }

      try {
        return DataHash.fromImprint(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, DataHash.class, "Expected bytes");
      }
    }
  }
}

