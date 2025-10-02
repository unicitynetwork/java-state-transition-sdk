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
import java.math.BigInteger;

/**
 * Sparse merkle tree path step serializer and deserializer implementation.
 */
public class SparseMerkleTreePathStepJson {

  private SparseMerkleTreePathStepJson() {
  }

  /**
   * Sparse merkle tree path step serializer.
   */
  public static class Serializer extends StdSerializer<SparseMerkleTreePathStep> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(SparseMerkleTreePathStep.class);
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
    public void serialize(SparseMerkleTreePathStep value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeStartArray();
      gen.writeObject(value.getPath().toString());
      if (value.getSibling().isPresent()) {
        gen.writeStartArray();
        gen.writeObject(value.getSibling().get().getValue());
        gen.writeEndArray();
      } else {
        gen.writeNull();
      }
      if (value.getBranch().isPresent()) {
        gen.writeStartArray();
        gen.writeObject(value.getBranch().get().getValue());
        gen.writeEndArray();
      } else {
        gen.writeNull();
      }
      gen.writeEndArray();
    }
  }

  /**
   * Sparse merkle tree path step deserializer.
   */
  public static class Deserializer extends StdDeserializer<SparseMerkleTreePathStep> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(SparseMerkleTreePathStep.class);
    }

    private static SparseMerkleTreePathStep.Branch parseBranch(JsonParser p) throws IOException {
      p.nextToken();

      if (p.currentToken() == JsonToken.VALUE_NULL) {
        return null;
      }

      if (p.currentToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleTreePathStep.Branch.class,
            "Expected start of array"
        );
      }
      p.nextToken();
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

    /**
     * Deserialize sparse merkle tree path step.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return sparse merkle tree path step
     * @throws IOException on deserialization failure
     */
    @Override
    public SparseMerkleTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleTreePathStep.class,
            "Expected start of array"
        );
      }
      p.nextToken();

      SparseMerkleTreePathStep step = new SparseMerkleTreePathStep(
          new BigInteger(p.readValueAs(String.class)),
          SparseMerkleTreePathStepJson
              .Deserializer.parseBranch(p),
          SparseMerkleTreePathStepJson
              .Deserializer.parseBranch(p));

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleTreePathStep.class,
            "Expected end of array"
        );
      }

      return step;
    }
  }
}

