package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

public class TokenTypeJson {

  private TokenTypeJson() {
  }

  public static class Serializer extends StdSerializer<TokenType> {

    public Serializer() {
      super(TokenType.class);
    }

    @Override
    public void serialize(TokenType value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeObject(value.getBytes());
    }
  }

  public static class Deserializer extends StdDeserializer<TokenType> {

    public Deserializer() {
      super(TokenType.class);
    }

    @Override
    public TokenType deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            TokenType.class,
            "Expected string value"
        );
      }

      try {
        return new TokenType(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, TokenType.class, "Expected bytes");
      }
    }
  }
}

