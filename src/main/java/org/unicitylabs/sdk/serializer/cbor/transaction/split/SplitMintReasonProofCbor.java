package org.unicitylabs.sdk.serializer.cbor.transaction.split;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePath;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.transaction.split.SplitMintReasonProof;
import java.io.IOException;

public class SplitMintReasonProofCbor {

  private SplitMintReasonProofCbor() {
  }


  public static class Serializer extends JsonSerializer<SplitMintReasonProof> {

    public Serializer() {
    }

    @Override
    public void serialize(SplitMintReasonProof value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 3);
      gen.writeObject(value.getCoinId().getBytes());
      gen.writeObject(value.getAggregationPath());
      gen.writeObject(value.getCoinTreePath());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SplitMintReasonProof> {

    public Deserializer() {
    }

    @Override
    public SplitMintReasonProof deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, SplitMintReasonProof.class, "Expected array");
      }

      return new SplitMintReasonProof(
          new CoinId(p.readValueAs(byte[].class)),
          p.readValueAs(SparseMerkleTreePath.class),
          p.readValueAs(SparseMerkleSumTreePath.class)
      );
    }
  }

}
