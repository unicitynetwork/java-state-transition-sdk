package com.unicity.sdk.serializer.json.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePath;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SparseMerkleSumTreePathJson {

  private static final String ROOT_HASH_FIELD = "root";
  private static final String ROOT_COUNTER_FIELD = "sum";
  private static final String STEPS_FIELD = "steps";

  private SparseMerkleSumTreePathJson() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleSumTreePath> {

    @Override
    public void serialize(SparseMerkleSumTreePath value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(ROOT_HASH_FIELD, value.getRoot().getHash());
      gen.writeObjectField(ROOT_COUNTER_FIELD, value.getRoot().getCounter());
      gen.writeObjectField(STEPS_FIELD, value.getSteps());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleSumTreePath> {

    @Override
    public SparseMerkleSumTreePath deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      DataHash hash = null;
      BigInteger counter = null;
      List<SparseMerkleSumTreePathStep> steps = new ArrayList<>();
      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePath.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, SparseMerkleSumTreePath.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case ROOT_HASH_FIELD:
              hash = p.readValueAs(DataHash.class);
              break;
            case ROOT_COUNTER_FIELD:
              counter = new BigInteger(p.readValueAs(String.class));
              break;
            case STEPS_FIELD:
              if (!p.isExpectedStartArrayToken()) {
                throw MismatchedInputException.from(p, SparseMerkleSumTreePath.class,
                    "Expected array value");
              }

              while (p.nextToken() != JsonToken.END_ARRAY) {
                steps.add(p.readValueAs(SparseMerkleSumTreePathStep.class));
              }
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, SparseMerkleSumTreePath.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(ROOT_HASH_FIELD, STEPS_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePath.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new SparseMerkleSumTreePath(new SparseMerkleSumTreePath.Root(hash, counter), steps);
    }
  }
}
