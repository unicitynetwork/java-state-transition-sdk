package org.unicitylabs.sdk.serializer.cbor.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import java.io.IOException;

public class SparseMerkleSumTreePathStepCbor {

  private SparseMerkleSumTreePathStepCbor() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleSumTreePathStep> {

    @Override
    public void serialize(SparseMerkleSumTreePathStep value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 3);
      gen.writeObject(BigIntegerConverter.encode(value.getPath()));
      gen.writeObject(value.getSibling());
      gen.writeObject(value.getBranch());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleSumTreePathStep> {

    @Override
    public SparseMerkleSumTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
            "Expected array");
      }
      p.nextToken();

      return new SparseMerkleSumTreePathStep(
          BigIntegerConverter.decode(p.readValueAs(byte[].class)),
          p.readValueAs(SparseMerkleSumTreePathStep.Branch.class),
          p.readValueAs(SparseMerkleSumTreePathStep.Branch.class)
      );
    }
  }
}

