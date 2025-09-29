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
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

public class TokenIdJson {

  private TokenIdJson() {
  }

  public static class Serializer extends StdSerializer<TokenId> {

    public Serializer() {
      super(TokenId.class);
    }

    @Override
    public void serialize(TokenId value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeObject(value.getBytes());
    }
  }

  public static class Deserializer extends StdDeserializer<TokenId> {

    public Deserializer() {
      super(TokenId.class);
    }

    @Override
    public TokenId deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            TokenId.class,
            "Expected string value"
        );
      }

      try {
        return new TokenId(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, TokenId.class, "Expected bytes");
      }
    }
  }
}

