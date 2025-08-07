package com.unicity.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.BlockHeightResponse;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class SubmitCommitmentResponseJson {

  private static final String STATUS_FIELD = "status";

  private SubmitCommitmentResponseJson() {
  }

  public static class Deserializer extends JsonDeserializer<SubmitCommitmentResponse> {

    public Deserializer() {
    }


    @Override
    public SubmitCommitmentResponse deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      SubmitCommitmentStatus status = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SubmitCommitmentResponse.class,
            "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, SubmitCommitmentResponse.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case STATUS_FIELD:
              if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, SubmitCommitmentResponse.class,
                    "Expected string value");
              }
              status = SubmitCommitmentStatus.valueOf(p.getValueAsString());
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, SubmitCommitmentResponse.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(STATUS_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, SubmitCommitmentResponse.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new SubmitCommitmentResponse(status);
    }
  }
}
