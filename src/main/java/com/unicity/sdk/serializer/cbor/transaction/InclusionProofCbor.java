package com.unicity.sdk.serializer.cbor.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePath;
import com.unicity.sdk.transaction.InclusionProof;
import java.io.IOException;

public class InclusionProofCbor {

  private InclusionProofCbor() {
  }

  public static class Serializer extends JsonSerializer<InclusionProof> {

    @Override
    public void serialize(InclusionProof value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 3);
      gen.writeObject(value.getMerkleTreePath());
      gen.writeObject(value.getAuthenticator());
      gen.writeObject(value.getTransactionHash());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<InclusionProof> {

    @Override
    public InclusionProof deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, InclusionProof.class, "Expected array value");
      }

      return new InclusionProof(
          p.readValueAs(SparseMerkleTreePath.class),
          p.readValueAs(Authenticator.class),
          p.readValueAs(DataHash.class)
      );
    }
  }
}
