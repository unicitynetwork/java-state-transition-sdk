package org.unicitylabs.sdk.mtree.sum;

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
public class SparseMerkleSumTreePathStepBranchJson {

  private SparseMerkleSumTreePathStepBranchJson() {
  }

  /**
   * Sparse merkle tree path step serializer.
   */
  public static class Serializer extends StdSerializer<SparseMerkleSumTreePathStep.Branch> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(SparseMerkleSumTreePathStep.Branch.class);
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
    public void serialize(SparseMerkleSumTreePathStep.Branch value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeStartArray();
      gen.writeObject(value.getValue());
      gen.writeObject(value.getCounter().toString());
      gen.writeEndArray();
    }
  }

  /**
   * Sparse merkle tree path step deserializer.
   */
  public static class Deserializer extends StdDeserializer<SparseMerkleSumTreePathStep.Branch> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(SparseMerkleSumTreePathStep.Branch.class);
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
    public SparseMerkleSumTreePathStep.Branch deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.currentToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleSumTreePathStep.Branch.class,
            "Expected start of array"
        );
      }

      if (p.nextToken() == JsonToken.END_ARRAY) {
        return new SparseMerkleSumTreePathStep.Branch(null, BigInteger.ZERO);
      }

      SparseMerkleSumTreePathStep.Branch branch = new SparseMerkleSumTreePathStep.Branch(
          p.readValueAs(byte[].class),
          p.readValueAs(BigInteger.class)
      );

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleSumTreePathStep.Branch.class,
            "Expected end of array"
        );
      }

      return branch;
    }
  }
}

