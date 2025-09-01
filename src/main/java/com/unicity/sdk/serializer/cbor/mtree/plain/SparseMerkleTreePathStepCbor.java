package com.unicity.sdk.serializer.cbor.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import com.unicity.sdk.util.BigIntegerConverter;
import java.io.IOException;

public class SparseMerkleTreePathStepCbor {

  private SparseMerkleTreePathStepCbor() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleTreePathStep> {

    @Override
    public void serialize(SparseMerkleTreePathStep value, JsonGenerator gen,
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

  public static class Deserializer extends JsonDeserializer<SparseMerkleTreePathStep> {

    @Override
    public SparseMerkleTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
            "Expected array value");
      }
      p.nextToken();

      return new SparseMerkleTreePathStep(
          BigIntegerConverter.decode(p.readValueAs(byte[].class)),
          p.readValueAs(SparseMerkleTreePathStep.Branch.class),
          p.readValueAs(SparseMerkleTreePathStep.Branch.class)
      );
    }
  }
}

