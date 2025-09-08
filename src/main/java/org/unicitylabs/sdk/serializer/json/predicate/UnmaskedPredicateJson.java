package org.unicitylabs.sdk.serializer.json.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateType;
import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UnmaskedPredicateJson {

  private static final String TYPE_FIELD = "type";
  private static final String PUBLIC_KEY_FIELD = "publicKey";
  private static final String ALGORITHM_FIELD = "algorithm";
  private static final String HASH_ALGORITHM_FIELD = "hashAlgorithm";
  private static final String NONCE_FIELD = "nonce";

  private UnmaskedPredicateJson() {
  }

  public static class Serializer extends JsonSerializer<UnmaskedPredicate> {

    @Override
    public void serialize(UnmaskedPredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(TYPE_FIELD, value.getType());
      gen.writeObjectField(PUBLIC_KEY_FIELD, value.getPublicKey());
      gen.writeObjectField(ALGORITHM_FIELD, value.getSigningAlgorithm());
      gen.writeObjectField(HASH_ALGORITHM_FIELD, value.getHashAlgorithm().getValue());
      gen.writeObjectField(NONCE_FIELD, value.getNonce());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<UnmaskedPredicate> {

    @Override
    public UnmaskedPredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      PredicateType type = null;
      byte[] publicKey = null;
      String algorithm = null;
      HashAlgorithm hashAlgorithm = null;
      byte[] nonce = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, UnmaskedPredicate.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, UnmaskedPredicate.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case TYPE_FIELD:
              type = PredicateType.valueOf(p.readValueAs(String.class));
              if (type != PredicateType.UNMASKED) {
                throw MismatchedInputException.from(p, UnmaskedPredicate.class,
                    String.format("Expected type to be %s, but got %s", PredicateType.MASKED,
                        type));
              }
              break;
            case PUBLIC_KEY_FIELD:
              publicKey = p.readValueAs(byte[].class);
              break;
            case ALGORITHM_FIELD:
              if (p.currentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, UnmaskedPredicate.class,
                    "Expected algorithm to be a string");
              }
              algorithm = p.readValueAs(String.class);
              break;
            case HASH_ALGORITHM_FIELD:
              if (p.currentToken() != JsonToken.VALUE_NUMBER_INT) {
                throw MismatchedInputException.from(p, UnmaskedPredicate.class,
                    "Expected hashAlgorithm to be a string");
              }
              hashAlgorithm = HashAlgorithm.fromValue(p.readValueAs(Integer.class));
              break;
            case NONCE_FIELD:
              nonce = p.readValueAs(byte[].class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, UnmaskedPredicate.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(TYPE_FIELD, PUBLIC_KEY_FIELD, ALGORITHM_FIELD, HASH_ALGORITHM_FIELD, NONCE_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, UnmaskedPredicate.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new UnmaskedPredicate(publicKey, algorithm, hashAlgorithm, nonce);
    }
  }
}
