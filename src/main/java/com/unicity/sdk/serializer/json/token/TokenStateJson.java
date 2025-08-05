package com.unicity.sdk.serializer.json.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.predicate.Predicate;
import com.unicity.sdk.token.TokenState;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TokenStateJson {

  private static final String UNLOCK_PREDICATE_FIELD = "unlockPredicate";
  private static final String DATA_FIELD = "data";

  private TokenStateJson() {
  }

  public static class Serializer extends JsonSerializer<TokenState> {

    @Override
    public void serialize(TokenState value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(UNLOCK_PREDICATE_FIELD, value.getUnlockPredicate());
      gen.writeObjectField(DATA_FIELD, value.getData());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<TokenState> {

    @Override
    public TokenState deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      Predicate predicate = null;
      byte[] data = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, TokenState.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, TokenState.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case UNLOCK_PREDICATE_FIELD:
              predicate = p.readValueAs(Predicate.class);
              break;
            case DATA_FIELD:
              data =
                  p.getCurrentToken() != JsonToken.VALUE_NULL ? p.readValueAs(byte[].class) : null;
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, TokenState.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(UNLOCK_PREDICATE_FIELD, DATA_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, TokenState.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new TokenState(predicate, data);
    }
  }
}
