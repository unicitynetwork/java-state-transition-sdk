package com.unicity.sdk.serializer.cbor.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePath;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import com.unicity.sdk.util.BigIntegerConverter;
import java.io.IOException;
import java.util.List;

public class SparseMerkleSumTreePathCbor {

  private SparseMerkleSumTreePathCbor() {
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

      gen.writeStartArray(value, 3);
      gen.writeObject(value.getRoot().getHash());
      gen.writeObject(BigIntegerConverter.encode(value.getRoot().getCounter()));
      gen.writeObject(value.getSteps());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleSumTreePath> {

    @Override
    public SparseMerkleSumTreePath deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePath.class,
            "Expected object value");
      }

      return new SparseMerkleSumTreePath(
          new SparseMerkleSumTreePath.Root(
              p.readValueAs(DataHash.class),
              BigIntegerConverter.decode(p.readValueAs(byte[].class))
          ),
          ctx.readValue(p, ctx.getTypeFactory()
              .constructCollectionType(List.class, SparseMerkleSumTreePathStep.class)));
    }
  }
}
