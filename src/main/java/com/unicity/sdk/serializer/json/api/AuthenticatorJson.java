package com.unicity.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.signing.Signature;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AuthenticatorJson {

  private static final String ALGORITHM_FIELD = "algorithm";
  private static final String PUBLIC_KEY_FIELD = "publicKey";
  private static final String SIGNATURE_FIELD = "signature";
  private static final String STATE_HASH_FIELD = "stateHash";

  private AuthenticatorJson() {
  }

  public static class Serializer extends JsonSerializer<Authenticator> {

    @Override
    public void serialize(Authenticator value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(ALGORITHM_FIELD, value.getAlgorithm());
      gen.writeObjectField(PUBLIC_KEY_FIELD, HexConverter.encode(value.getPublicKey()));
      gen.writeObjectField(SIGNATURE_FIELD, HexConverter.encode(value.getSignature().encode()));
      gen.writeObjectField(STATE_HASH_FIELD, value.getStateHash());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<Authenticator> {

    @Override
    public Authenticator deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      String algorithm = null;
      byte[] publicKey = null;
      Signature signature = null;
      DataHash stateHash = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, Authenticator.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, Authenticator.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
          throw MismatchedInputException.from(p, Authenticator.class, "Expected string value");
        }

        try {
          switch (fieldName) {
            case ALGORITHM_FIELD:
              algorithm = p.readValueAs(String.class);
              break;
            case PUBLIC_KEY_FIELD:
              publicKey = p.readValueAs(byte[].class);
              break;
            case SIGNATURE_FIELD:
              signature = Signature.decode(p.readValueAs(byte[].class));
              break;
            case STATE_HASH_FIELD:
              stateHash = p.readValueAs(DataHash.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, Authenticator.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(ALGORITHM_FIELD, PUBLIC_KEY_FIELD, SIGNATURE_FIELD, STATE_HASH_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, Authenticator.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new Authenticator(algorithm, publicKey, signature, stateHash);
    }
  }
}
