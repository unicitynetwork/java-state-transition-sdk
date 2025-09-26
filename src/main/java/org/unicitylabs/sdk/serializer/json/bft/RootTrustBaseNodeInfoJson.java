package org.unicitylabs.sdk.serializer.json.bft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.unicitylabs.sdk.bft.RootTrustBase;

public class RootTrustBaseNodeInfoJson {
  private static final String NODE_ID_FIELD = "nodeId";
  private static final String SIGNING_KEY_FIELD = "sigKey";
  private static final String STAKED_AMOUNT_FIELD = "stake";

  private RootTrustBaseNodeInfoJson() {
  }

  public static class Serializer extends JsonSerializer<RootTrustBase.NodeInfo> {

    @Override
    public void serialize(RootTrustBase.NodeInfo value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(NODE_ID_FIELD, value.getNodeId());
      gen.writeObjectField(SIGNING_KEY_FIELD, value.getSigningKey());
      gen.writeObjectField(STAKED_AMOUNT_FIELD, value.getStakedAmount());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<RootTrustBase.NodeInfo> {

    @Override
    public RootTrustBase.NodeInfo deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      String nodeId = null;
      byte[] signingKey = null;
      long stakedAmount = 0;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, RootTrustBase.NodeInfo.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, RootTrustBase.NodeInfo.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();

        try {
          switch (fieldName) {
            case NODE_ID_FIELD:
              nodeId = p.readValueAs(String.class);
              break;
            case SIGNING_KEY_FIELD:
              signingKey = p.readValueAs(byte[].class);
              break;
            case STAKED_AMOUNT_FIELD:
              stakedAmount = p.readValueAs(Long.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, RootTrustBase.NodeInfo.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(NODE_ID_FIELD, SIGNING_KEY_FIELD, STAKED_AMOUNT_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, RootTrustBase.NodeInfo.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new RootTrustBase.NodeInfo(nodeId, signingKey, stakedAmount);
    }
  }
}
