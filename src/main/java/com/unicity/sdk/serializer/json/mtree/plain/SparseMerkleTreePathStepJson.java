package com.unicity.sdk.serializer.json.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class SparseMerkleTreePathStepJson {

  private static final String PATH_FIELD = "path";
  private static final String SIBLING_FIELD = "sibling";
  private static final String BRANCH_FIELD = "branch";

  private SparseMerkleTreePathStepJson() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleTreePathStep> {

    @Override
    public void serialize(SparseMerkleTreePathStep value, JsonGenerator gen,
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

  public static class Deserializer extends JsonDeserializer<SparseMerkleTreePathStep> {

    @Override
    public SparseMerkleTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      BigInteger path = null;
      SparseMerkleTreePathStep.Branch sibling = null;
      SparseMerkleTreePathStep.Branch branch = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
            "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case PATH_FIELD:
              if (p.currentToken() != JsonToken.VALUE_STRING) {
                throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
                    "Expected string value");
              }
              path = new BigInteger(p.readValueAs(String.class));
              break;
            case SIBLING_FIELD:
              sibling = p.readValueAs(SparseMerkleTreePathStep.Branch.class);
              break;
            case BRANCH_FIELD:
              branch = p.readValueAs(SparseMerkleTreePathStep.Branch.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, SparseMerkleTreePathStep.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(PATH_FIELD, SIBLING_FIELD, BRANCH_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new SparseMerkleTreePathStep(path, sibling, branch);
    }
  }
}

