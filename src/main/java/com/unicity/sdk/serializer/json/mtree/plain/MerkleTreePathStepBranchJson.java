package com.unicity.sdk.serializer.json.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStepBranch;
import com.unicity.sdk.util.HexConverter;

import java.io.IOException;

public class MerkleTreePathStepBranchJson {

  private MerkleTreePathStepBranchJson() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleTreePathStepBranch> {

    @Override
    public void serialize(SparseMerkleTreePathStepBranch value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      String[] result = value.getValue() == null ? new String[0]
          : new String[]{HexConverter.encode(value.getValue())};
      gen.writeArray(result, 0, result.length);
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleTreePathStepBranch> {

    @Override
    public SparseMerkleTreePathStepBranch deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStepBranch.class,
            "Expected array value");
      }

      try {
        byte[] value = null;
        if (p.nextToken() != JsonToken.END_ARRAY) {
          value = p.currentToken() != JsonToken.VALUE_NULL ? p.readValueAs(byte[].class) : null;
          if (p.nextToken() != JsonToken.END_ARRAY) {
            throw MismatchedInputException.from(p, SparseMerkleTreePathStepBranch.class,
                "Expected only one element in array");
          }
        }

        return new SparseMerkleTreePathStepBranch(value);
      } catch (Exception e) {
        throw MismatchedInputException.from(p, SparseMerkleTreePathStepBranch.class,
            "Expected hex string value");
      }
    }
  }
}
