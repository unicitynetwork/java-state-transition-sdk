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
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;

/**
 * Sparse merkle sum tree path step serializer and deserializer implementation.
 */
public class SparseMerkleSumTreePathStepJson {

  private SparseMerkleSumTreePathStepJson() {
  }

  /**
   * Sparse merkle sum tree path step serializer.
   */
  public static class Serializer extends StdSerializer<SparseMerkleSumTreePathStep> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(SparseMerkleSumTreePathStep.class);
    }

    /**
     * Serialize sparse merkle sum tree path step.
     *
     * @param value       path step
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(SparseMerkleSumTreePathStep value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeStartArray();
      gen.writeObject(value.getPath().toString());
      SparseMerkleSumTreePathStep.Branch sibling = value.getSibling().orElse(null);
      if (sibling != null) {
        gen.writeStartArray();
        gen.writeObject(sibling.getValue());
        gen.writeObject(sibling.getCounter().toString());
        gen.writeEndArray();
      } else {
        gen.writeNull();
      }

      SparseMerkleSumTreePathStep.Branch branch = value.getBranch().orElse(null);
      if (branch != null) {
        gen.writeStartArray();
        gen.writeObject(branch.getValue());
        gen.writeObject(branch.getCounter().toString());
        gen.writeEndArray();
      } else {
        gen.writeNull();
      }
      gen.writeEndArray();
    }
  }

  /**
   * Sparse merkle sum tree path step deserializer.
   */
  public static class Deserializer extends StdDeserializer<SparseMerkleSumTreePathStep> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(SparseMerkleSumTreePathStep.class);
    }

    /**
     * Deserialize sparse merkle sum tree path step branch.
     *
     * @param p Parser used for reading JSON content
     * @return branch
     * @throws IOException on deserialization failure
     */
    public static SparseMerkleSumTreePathStep.Branch parseBranch(JsonParser p) throws IOException {
      p.nextToken();

      if (p.currentToken() == JsonToken.VALUE_NULL) {
        return null;
      }

      if (p.currentToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleSumTreePathStep.Branch.class,
            "Expected start of array"
        );
      }
      p.nextToken();
      SparseMerkleSumTreePathStep.Branch branch = new SparseMerkleSumTreePathStep.Branch(
          p.readValueAs(byte[].class),
          new BigInteger(p.readValueAs(String.class))
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

    /**
     * Deserialize sparse merkle sum tree path step.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return path step
     * @throws IOException on deserialization failure
     */
    @Override
    public SparseMerkleSumTreePathStep deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.START_ARRAY) {
        throw MismatchedInputException.from(
            p,
            SparseMerkleTreePathStep.class,
            "Expected start of array"
        );
      }
      p.nextToken();

      SparseMerkleSumTreePathStep step = new SparseMerkleSumTreePathStep(
          new BigInteger(p.readValueAs(String.class)),
          SparseMerkleSumTreePathStepJson
              .Deserializer.parseBranch(p),
          SparseMerkleSumTreePathStepJson
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

