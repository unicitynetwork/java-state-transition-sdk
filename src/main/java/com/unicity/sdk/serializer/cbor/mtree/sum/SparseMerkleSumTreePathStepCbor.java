package com.unicity.sdk.serializer.cbor.mtree.sum;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep;
import com.unicity.sdk.util.BigIntegerConverter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
      gen.writeObject(value.getSibling()
          .map(branch ->
              List.of(BigIntegerConverter.encode(branch.getCounter()), branch.getValue())
          )
          .orElse(null));
      gen.writeObject(value.getBranch()
          .map(branch -> {
                ArrayList<Object> data = new ArrayList<>();
                data.add(BigIntegerConverter.encode(branch.getCounter()));
                data.add(branch.getValue());
                return data;
              }
          )
          .orElse(null));

      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleSumTreePathStep> {

    @Override
    public SparseMerkleSumTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
            "Expected object value");
      }

      return new SparseMerkleSumTreePathStep(
          BigIntegerConverter.decode(p.readValueAs(byte[].class)),
          this.readBranch(p),
          this.readBranch(p)
      );
    }

    private SparseMerkleSumTreePathStep.Branch readBranch(JsonParser p) throws IOException {
      if (p.currentToken() == JsonToken.VALUE_NULL) {
        return null;
      }

      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
            "Expected array");
      }
      p.nextToken();

      BigInteger counter = new BigInteger(p.readValueAs(byte[].class));
      byte[] value = p.readValueAs(byte[].class);

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, SparseMerkleSumTreePathStep.class,
            "Expected array end");
      }

      return new SparseMerkleSumTreePathStep.Branch(value, counter);
    }
  }
}

