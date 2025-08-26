package com.unicity.sdk.serializer.cbor.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStepBranch;
import com.unicity.sdk.util.BigIntegerConverter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

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
      gen.writeObject(value.getSibling().map(DataHash::getImprint).orElse(null));
      gen.writeObject(value.getBranch()
          .map(branch -> Collections.singletonList(branch.getValue()))
          .orElse(null)
      );
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleTreePathStep> {

    @Override
    public SparseMerkleTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
            "Expected object value");
      }

      BigInteger path = BigIntegerConverter.decode(p.readValueAs(byte[].class));
      DataHash sibling = p.readValueAs(DataHash.class);
      List<byte[]> value = ctx.readValue(p,
          ctx.getTypeFactory().constructCollectionType(List.class, byte[].class));
      if (value != null && value.isEmpty()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStep.class,
            "Expected branch value");
      }
      return new SparseMerkleTreePathStep(
          path,
          sibling,
          value == null ? null : new SparseMerkleTreePathStepBranch(value.get(0))
      );
    }
  }
}

