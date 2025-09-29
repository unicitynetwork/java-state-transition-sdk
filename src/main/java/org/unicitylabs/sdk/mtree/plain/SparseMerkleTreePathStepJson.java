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

public class SparseMerkleTreePathStepJson {

  private SparseMerkleTreePathStepJson() {
  }

  public static class Serializer extends StdSerializer<SparseMerkleTreePathStep> {

    public Serializer() {
      super(SparseMerkleTreePathStep.class);
    }

    @Override
    public void serialize(SparseMerkleTreePathStep value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

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

  public static class Deserializer extends StdDeserializer<SparseMerkleTreePathStep> {

    public Deserializer() {
      super(SparseMerkleTreePathStep.class);
    }

    public static SparseMerkleTreePathStep.Branch parseBranch(JsonParser p) throws IOException {
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

