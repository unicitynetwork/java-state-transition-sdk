package org.unicitylabs.sdk.serializer.json.transaction.split;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
import java.util.HashSet;
import java.util.Set;

public class SplitMintReasonProofJson {

  private static final String COIN_ID_FIELD = "coinId";
  private static final String AGGREGATION_PATH_FIELD = "aggregationPath";
  private static final String COIN_TREE_PATH_FIELD = "coinTreePath";

  private SplitMintReasonProofJson() {
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

      gen.writeStartObject();
      gen.writeObjectField(COIN_ID_FIELD, value.getCoinId().getBytes());
      gen.writeObjectField(AGGREGATION_PATH_FIELD, value.getAggregationPath());
      gen.writeObjectField(COIN_TREE_PATH_FIELD, value.getCoinTreePath());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<SplitMintReasonProof> {

    public Deserializer() {
    }

    @Override
    public SplitMintReasonProof deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, SplitMintReasonProof.class, "Expected object value");
      }

      CoinId coinId = null;
      SparseMerkleTreePath aggregationPath = null;
      SparseMerkleSumTreePath coinTreePath = null;

      Set<String> fields = new HashSet<>();

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, SplitMintReasonProof.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case COIN_ID_FIELD:
              coinId = new CoinId(p.readValueAs(byte[].class));
              break;
            case AGGREGATION_PATH_FIELD:
              aggregationPath = p.readValueAs(SparseMerkleTreePath.class);
              break;
            case COIN_TREE_PATH_FIELD:
              coinTreePath = p.readValueAs(SparseMerkleSumTreePath.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, SplitMintReasonProof.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(COIN_ID_FIELD, AGGREGATION_PATH_FIELD, COIN_TREE_PATH_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, SplitMintReasonProof.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new SplitMintReasonProof(coinId, aggregationPath, coinTreePath);
    }
  }

}
