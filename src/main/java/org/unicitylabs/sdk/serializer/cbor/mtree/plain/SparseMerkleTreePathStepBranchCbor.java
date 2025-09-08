package org.unicitylabs.sdk.serializer.cbor.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep.Branch;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import java.io.IOException;

public class SparseMerkleTreePathStepBranchCbor {

  private SparseMerkleTreePathStepBranchCbor() {
  }

  public static class Serializer extends JsonSerializer<Branch> {

    @Override
    public void serialize(Branch value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 1);
      gen.writeObject(value.getValue());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<Branch> {

    @Override
    public Branch deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.Branch.class,
            "Expected array");
      }
      if (p.nextToken() == JsonToken.END_ARRAY) {
        return new Branch(null);
      }

      Branch branch = new Branch(p.readValueAs(byte[].class));
      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.Branch.class,
            "Expected end of array");
      }

      return branch;
    }
  }
}

