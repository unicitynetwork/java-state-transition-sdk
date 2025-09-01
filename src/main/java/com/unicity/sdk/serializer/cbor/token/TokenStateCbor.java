package com.unicity.sdk.serializer.cbor.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.predicate.Predicate;
import com.unicity.sdk.token.TokenState;
import java.io.IOException;

public class TokenStateCbor {

  private TokenStateCbor() {
  }

  public static class Serializer extends JsonSerializer<TokenState> {

    @Override
    public void serialize(TokenState value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 2);
      gen.writeObject(value.getUnlockPredicate());
      gen.writeObject(value.getData());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<TokenState> {

    @Override
    public TokenState deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, TokenState.class, "Expected array");
      }

      return new TokenState(p.readValueAs(Predicate.class), p.readValueAs(byte[].class));
    }
  }
}
