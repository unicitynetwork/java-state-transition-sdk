package org.unicitylabs.sdk.serializer.cbor.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePathStep.Branch;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import java.io.IOException;

public class SparseMerkleSumTreePathStepBranchCbor {

  private SparseMerkleSumTreePathStepBranchCbor() {
  }

  public static class Serializer extends JsonSerializer<Branch> {

    @Override
    public void serialize(Branch value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 2);
      gen.writeObject(value.getValue());
      gen.writeObject(BigIntegerConverter.encode(value.getCounter()));
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
          p.readValueAs(byte[].class),
          BigIntegerConverter.decode(p.readValueAs(byte[].class))
      );
      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, Branch.class,
            "Expected end of array");
      }

      return branch;
    }
  }
}

