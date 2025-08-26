package com.unicity.sdk.serializer.json.transaction.split;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.transaction.split.SplitMintReason;
import com.unicity.sdk.transaction.split.SplitMintReasonProof;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SplitMintReasonJson {
  private static final String TOKEN_FIELD = "token";
  private static final String PROOFS_FIELD = "proofs";

  private SplitMintReasonJson() {
  }


  public static class Serializer extends JsonSerializer<SplitMintReason> {
    public Serializer() {
    }

    @Override
    public void serialize(SplitMintReason value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(TOKEN_FIELD, value.getToken());
      gen.writeObjectField(PROOFS_FIELD, value.getProofs());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<SplitMintReason> {

    public Deserializer() {
    }

    @Override
    public SplitMintReason deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      Token<?> token = null;
      List<SplitMintReasonProof> proofs = new ArrayList<>();

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SplitMintReason.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, SplitMintReason.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case TOKEN_FIELD:
              token = p.readValueAs(Token.class);
              break;
            case PROOFS_FIELD:
              if (!p.isExpectedStartArrayToken()) {
                throw MismatchedInputException.from(p, SplitMintReason.class, "Expected array value");
              }

              while (p.nextToken() != JsonToken.END_ARRAY) {
                proofs.add(p.readValueAs(SplitMintReasonProof.class));
              }
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, SplitMintReason.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(TOKEN_FIELD, PROOFS_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, SplitMintReason.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new SplitMintReason(token, proofs);
    }
  }

}
