package org.unicitylabs.sdk.serializer.json.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.token.TokenType;
import java.io.IOException;

public class TokenTypeJson {

  private TokenTypeJson() {
  }


  public static class Serializer extends JsonSerializer<TokenType> {

    public Serializer() {
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

  public static class Deserializer extends JsonDeserializer<TokenType> {

    public Deserializer() {
    }

    @Override
    public TokenType deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      try {
        return new TokenType(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, TokenType.class, "Expected bytes");
      }
    }
  }
}

