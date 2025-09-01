package com.unicity.sdk.serializer.json.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep.Branch;
import java.io.IOException;
import java.math.BigInteger;

public class SparseMerkleSumTreePathStepBranchJson {

  private SparseMerkleSumTreePathStepBranchJson() {
  }

  public static class Serializer extends JsonSerializer<Branch> {

    @Override
    public void serialize(Branch value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray();
      gen.writeObject(value.getCounter().toString());
      gen.writeObject(value.getValue());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<Branch> {

    @Override
    public Branch deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, Branch.class,
            "Expected array");
      }
      p.nextToken();

      Branch branch = new Branch(
          new BigInteger(p.readValueAs(String.class)),
          p.readValueAs(byte[].class)
      );
      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, Branch.class,
            "Expected end of array");
      }

      return branch;
    }
  }
}

