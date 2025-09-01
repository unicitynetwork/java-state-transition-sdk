package com.unicity.sdk.serializer.json.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class SparseMerkleSumTreePathStepJson {

  private static final String PATH_FIELD = "path";
  private static final String SIBLING_FIELD = "sibling";
  private static final String BRANCH_FIELD = "branch";

  private SparseMerkleSumTreePathStepJson() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleSumTreePathStep> {

    @Override
    public void serialize(SparseMerkleSumTreePathStep value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeStringField(PATH_FIELD, value.getPath().toString());
      gen.writeObjectField(SIBLING_FIELD, value.getSibling());
      gen.writeObjectField(BRANCH_FIELD, value.getBranch());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleSumTreePathStep> {

    @Override
    public SparseMerkleSumTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      BigInteger path = null;
      SparseMerkleSumTreePathStep.Branch sibling = null;
      SparseMerkleSumTreePathStep.Branch branch = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
            "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case PATH_FIELD:
              if (p.currentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
                    "Expected string value");
              }
              path = new BigInteger(p.readValueAs(String.class));
              break;
            case SIBLING_FIELD:
              sibling = p.readValueAs(SparseMerkleSumTreePathStep.Branch.class);
              break;
            case BRANCH_FIELD:
              branch = p.readValueAs(SparseMerkleSumTreePathStep.Branch.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, SparseMerkleSumTreePathStep.class,
              fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(PATH_FIELD, SIBLING_FIELD, BRANCH_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new SparseMerkleSumTreePathStep(path, sibling, branch);
    }
  }
}

