package com.unicity.sdk.serializer.cbor.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.signing.Signature;
import java.io.IOException;

public class AuthenticatorCbor {

  private AuthenticatorCbor() {
  }

  public static class Serializer extends JsonSerializer<Authenticator> {

    @Override
    public void serialize(Authenticator value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 4);
      gen.writeObject(value.getAlgorithm());
      gen.writeObject(value.getPublicKey());
      gen.writeObject(value.getSignature().encode());
      gen.writeObject(value.getStateHash());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<Authenticator> {

    @Override
    public Authenticator deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, Authenticator.class, "Expected array value");
      }

      return new Authenticator(
          p.readValueAs(String.class),
          p.readValueAs(byte[].class),
          Signature.decode(p.readValueAs(byte[].class)),
          p.readValueAs(DataHash.class)
      );
    }
  }
}
