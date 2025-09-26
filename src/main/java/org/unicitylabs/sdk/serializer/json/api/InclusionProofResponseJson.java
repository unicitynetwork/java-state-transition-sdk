package org.unicitylabs.sdk.serializer.json.api;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.unicitylabs.sdk.api.InclusionProofResponse;
import org.unicitylabs.sdk.transaction.InclusionProof;

public class InclusionProofResponseJson {

  private static final String INCLUSION_PROOF_FIELD = "inclusionProof";

  private InclusionProofResponseJson() {
  }

  public static class Deserializer extends JsonDeserializer<InclusionProofResponse> {

    public Deserializer() {
    }


    @Override
    public InclusionProofResponse deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      InclusionProof inclusionProof = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, InclusionProofResponse.class,
            "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, InclusionProofResponse.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case INCLUSION_PROOF_FIELD:
              inclusionProof = p.readValueAs(InclusionProof.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, InclusionProofResponse.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(INCLUSION_PROOF_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, InclusionProofResponse.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new InclusionProofResponse(inclusionProof);
    }
  }
}
