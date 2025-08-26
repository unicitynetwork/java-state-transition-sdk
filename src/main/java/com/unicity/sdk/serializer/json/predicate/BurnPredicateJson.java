package com.unicity.sdk.serializer.json.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.predicate.BurnPredicate;
import com.unicity.sdk.predicate.PredicateType;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BurnPredicateJson {

  private static final String TYPE_FIELD = "type";
  private static final String NONCE_FIELD = "nonce";
  private static final String REASON_FIELD = "reason";

  private BurnPredicateJson() {
  }

  public static class Serializer extends JsonSerializer<BurnPredicate> {

    @Override
    public void serialize(BurnPredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(TYPE_FIELD, value.getType());
      gen.writeObjectField(REASON_FIELD, value.getReason());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<BurnPredicate> {

    @Override
    public BurnPredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      PredicateType type;
      byte[] nonce = null;
      DataHash reason = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, BurnPredicate.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, BurnPredicate.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case TYPE_FIELD:
              type = PredicateType.valueOf(p.readValueAs(String.class));
              if (type != PredicateType.BURN) {
                throw MismatchedInputException.from(p, BurnPredicate.class,
                    String.format("Expected type to be %s, but got %s", PredicateType.MASKED,
                        type));
              }
              break;
            case NONCE_FIELD:
              nonce = p.readValueAs(byte[].class);
              break;
            case REASON_FIELD:
              reason = p.readValueAs(DataHash.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, BurnPredicate.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(TYPE_FIELD, NONCE_FIELD, REASON_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, BurnPredicate.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new BurnPredicate(nonce, reason);
    }
  }
}
