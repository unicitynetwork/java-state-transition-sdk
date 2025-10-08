package org.unicitylabs.sdk.mtree.plain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Sparse merkle tree path step serializer and deserializer implementation.
 */
public class SparseMerkleTreePathStepBranchJson {

  private SparseMerkleTreePathStepBranchJson() {
  }

  /**
   * Sparse merkle tree path step serializer.
   */
  public static class Serializer extends StdSerializer<SparseMerkleTreePathStep.Branch> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(SparseMerkleTreePathStep.Branch.class);
    }

    /**
     * Serialize sparse merkle tree path step.
     *
     * @param value       sparse merkle tree path step
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(SparseMerkleTreePathStep.Branch value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeStartArray();
      gen.writeObject(value.getValue());
      gen.writeEndArray();
    }
  }

  /**
   * Sparse merkle tree path step deserializer.
   */
  public static class Deserializer extends StdDeserializer<SparseMerkleTreePathStep.Branch> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(SparseMerkleTreePathStep.Branch.class);
    }

    /**
     * Deserialize sparse merkle tree path step branch.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return sparse merkle tree path step
     * @throws IOException on deserialization failure
     */
    @Override
    public SparseMerkleTreePathStep.Branch deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.currentToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleTreePathStep.Branch.class,
            "Expected start of array"
        );
      }

      if (p.nextToken() == JsonToken.END_ARRAY) {
        return new SparseMerkleTreePathStep.Branch(null);
      }

      SparseMerkleTreePathStep.Branch branch = new SparseMerkleTreePathStep.Branch(
          p.readValueAs(byte[].class)
      );

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleTreePathStep.Branch.class,
            "Expected end of array"
        );
      }

      return branch;
    }
  }
}

