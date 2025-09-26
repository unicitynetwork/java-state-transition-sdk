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
import java.util.Map;
import java.util.Set;
import org.unicitylabs.sdk.bft.RootTrustBase;

public class RootTrustBaseJson {
  private static final String VERSION_FIELD = "version";
  private static final String NETWORK_ID_FIELD = "networkId";
  private static final String EPOCH_FIELD = "epoch";
  private static final String EPOCH_START_ROUND_FIELD = "epochStartRound";
  private static final String ROOT_NODES_FIELD = "rootNodes";
  private static final String QUORUM_THRESHOLD_FIELD = "quorumThreshold";
  private static final String STATE_HASH_FIELD = "stateHash";
  private static final String CHANGE_RECORD_HASH_FIELD = "changeRecordHash";
  private static final String PREVIOUS_ENTRY_HASH_FIELD = "previousEntryHash";
  private static final String SIGNATURES_FIELD = "signatures";

  private RootTrustBaseJson() {
  }

  public static class Serializer extends JsonSerializer<RootTrustBase> {

    @Override
    public void serialize(RootTrustBase value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(VERSION_FIELD, value.getVersion());
      gen.writeObjectField(NETWORK_ID_FIELD, value.getNetworkId());
      gen.writeObjectField(EPOCH_FIELD, value.getEpoch());
      gen.writeObjectField(EPOCH_START_ROUND_FIELD, value.getEpochStartRound());
      gen.writeObjectField(ROOT_NODES_FIELD, value.getRootNodes());
      gen.writeObjectField(QUORUM_THRESHOLD_FIELD, value.getQuorumThreshold());
      gen.writeObjectField(STATE_HASH_FIELD, value.getStateHash());
      gen.writeObjectField(CHANGE_RECORD_HASH_FIELD, value.getChangeRecordHash());
      gen.writeObjectField(PREVIOUS_ENTRY_HASH_FIELD, value.getPreviousEntryHash());
      gen.writeObjectField(SIGNATURES_FIELD, value.getSignatures());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<RootTrustBase> {

    @Override
    public RootTrustBase deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      Long version = null;
      Integer networkId = null;
      Long epoch = null;
      Long epochStartRound = null;
      Set<RootTrustBase.NodeInfo> rootNodes = null;
      Integer quorumThreshold = null;
      byte[] stateHash = null;
      byte[] changeRecordHash = null;
      byte[] previousEntryHash = null;
      Map<String, byte[]> signatures = null;


      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, RootTrustBase.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, RootTrustBase.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();

        try {
          switch (fieldName) {
            case VERSION_FIELD:
              version = p.readValueAs(Long.class);
              break;
            case NETWORK_ID_FIELD:
              networkId = p.readValueAs(Integer.class);
              break;
            case EPOCH_FIELD:
              epoch = p.readValueAs(Long.class);
              break;
            case EPOCH_START_ROUND_FIELD:
              epochStartRound = p.readValueAs(Long.class);
              break;
            case ROOT_NODES_FIELD:
              rootNodes = ctx.readValue(
                  p,
                  ctx.getTypeFactory().constructCollectionType(Set.class, RootTrustBase.NodeInfo.class)
              );
              break;
            case QUORUM_THRESHOLD_FIELD:
              quorumThreshold = p.readValueAs(Integer.class);
              break;
            case STATE_HASH_FIELD:
              stateHash = p.readValueAs(byte[].class);
              break;
            case CHANGE_RECORD_HASH_FIELD:
              changeRecordHash = p.readValueAs(byte[].class);
              break;
            case PREVIOUS_ENTRY_HASH_FIELD:
              previousEntryHash = p.readValueAs(byte[].class);
              break;
            case SIGNATURES_FIELD:
              signatures = ctx.readValue(
                  p,
                  ctx.getTypeFactory().constructMapType(Map.class, String.class, byte[].class)
              );
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, RootTrustBase.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(VERSION_FIELD, NETWORK_ID_FIELD, EPOCH_FIELD, EPOCH_START_ROUND_FIELD,
              ROOT_NODES_FIELD, QUORUM_THRESHOLD_FIELD, STATE_HASH_FIELD, CHANGE_RECORD_HASH_FIELD,
              PREVIOUS_ENTRY_HASH_FIELD, SIGNATURES_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, RootTrustBase.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new RootTrustBase(
          version,
          networkId,
          epoch,
          epochStartRound,
          rootNodes,
          quorumThreshold,
          stateHash,
          changeRecordHash,
          previousEntryHash,
          signatures
      );
    }
  }
}
