package com.unicity.sdk.serializer.cbor.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.token.TokenId;
import java.io.IOException;

public class TokenIdCbor {

  private TokenIdCbor() {
  }

  public static class Serializer extends JsonSerializer<TokenId> {

    @Override
    public void serialize(TokenId value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeBinary(value.getBytes());
    }
  }

  public static class Deserializer extends JsonDeserializer<TokenId> {

    @Override
    public TokenId deserialize(JsonParser p, DeserializationContext cxt) throws IOException {
      byte[] value = p.getBinaryValue();
      if (value == null) {
        throw MismatchedInputException.from(p, TokenId.class, "Expected byte value");
      }

      try {
        return new TokenId(value);
      } catch (Exception e) {
        throw MismatchedInputException.from(p, TokenId.class, "Expected byte value");
      }
    }
  }
}
