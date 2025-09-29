package org.unicitylabs.sdk.signing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.unicitylabs.sdk.token.TokenId;

public class SignatureJson {

  private SignatureJson() {
  }

  public static class Serializer extends StdSerializer<Signature> {

    public Serializer() {
      super(Signature.class);
    }

    @Override
    public void serialize(Signature value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeObject(value.encode());
    }
  }

  public static class Deserializer extends StdDeserializer<Signature> {

    public Deserializer() {
      super(Signature.class);
    }

    @Override
    public Signature deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            Signature.class,
            "Expected string value"
        );
      }

      try {
        return Signature.decode(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, Signature.class, "Expected bytes");
      }
    }
  }
}

