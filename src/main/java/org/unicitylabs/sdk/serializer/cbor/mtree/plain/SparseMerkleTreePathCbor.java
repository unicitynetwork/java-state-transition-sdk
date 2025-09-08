package org.unicitylabs.sdk.serializer.cbor.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;
import java.io.IOException;
import java.util.List;

public class SparseMerkleTreePathCbor {

  private SparseMerkleTreePathCbor() {
  }

  public static class Serializer extends JsonSerializer<SparseMerkleTreePath> {

    @Override
    public void serialize(SparseMerkleTreePath value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 2);
      gen.writeObject(value.getRootHash());
      gen.writeObject(value.getSteps());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SparseMerkleTreePath> {

    @Override
    public SparseMerkleTreePath deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SparseMerkleTreePath.class, "Expected object value");
      }

      return new SparseMerkleTreePath(
          p.readValueAs(DataHash.class),
          ctx.readValue(p, ctx.getTypeFactory()
              .constructCollectionType(List.class, SparseMerkleTreePathStep.class)));
    }
  }
}
