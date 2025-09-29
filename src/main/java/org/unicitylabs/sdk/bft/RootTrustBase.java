package org.unicitylabs.sdk.bft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.Token;

public class RootTrustBase {

  private final long version;
  private final int networkId;
  private final long epoch;
  private final long epochStartRound;
  private final Set<NodeInfo> rootNodes;
  private final long quorumThreshold;
  private final byte[] stateHash;
  private final byte[] changeRecordHash;
  private final byte[] previousEntryHash;
  private final Map<String, byte[]> signatures;

  @JsonCreator
  RootTrustBase(
      @JsonProperty("version") long version,
      @JsonProperty("networkId") int networkId,
      @JsonProperty("epoch") long epoch,
      @JsonProperty("epochStartRound") long epochStartRound,
      @JsonProperty("rootNodes") Set<NodeInfo> rootNodes,
      @JsonProperty("quorumThreshold") long quorumThreshold,
      @JsonProperty("stateHash") byte[] stateHash,
      @JsonProperty("changeRecordHash") byte[] changeRecordHash,
      @JsonProperty("previousEntryHash") byte[] previousEntryHash,
      @JsonProperty("signatures") Map<String, byte[]> signatures
  ) {
    this.version = version;
    this.networkId = networkId;
    this.epoch = epoch;
    this.epochStartRound = epochStartRound;
    this.rootNodes = Set.copyOf(rootNodes);
    this.quorumThreshold = quorumThreshold;
    this.stateHash = Arrays.copyOf(stateHash, stateHash.length);
    this.changeRecordHash = changeRecordHash == null
        ? null
        : Arrays.copyOf(changeRecordHash, changeRecordHash.length);
    this.previousEntryHash = previousEntryHash == null
        ? null
        : Arrays.copyOf(previousEntryHash, previousEntryHash.length);
    this.signatures = signatures.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> Arrays.copyOf(e.getValue(), e.getValue().length)
            )
        );
  }

  @JsonProperty("version")
  public long getVersion() {
    return this.version;
  }

  @JsonProperty("networkId")
  public int getNetworkId() {
    return this.networkId;
  }

  @JsonProperty("epoch")
  public long getEpoch() {
    return this.epoch;
  }

  @JsonProperty("epochStartRound")
  public long getEpochStartRound() {
    return this.epochStartRound;
  }

  @JsonProperty("rootNodes")
  public Set<NodeInfo> getRootNodes() {
    return this.rootNodes;
  }

  @JsonProperty("quorumThreshold")
  public long getQuorumThreshold() {
    return this.quorumThreshold;
  }

  @JsonProperty("stateHash")
  public byte[] getStateHash() {
    return Arrays.copyOf(this.stateHash, this.stateHash.length);
  }

  @JsonProperty("changeRecordHash")
  public byte[] getChangeRecordHash() {
    return this.changeRecordHash == null
        ? null
        : Arrays.copyOf(this.changeRecordHash, this.changeRecordHash.length);
  }

  @JsonProperty("previousEntryHash")
  public byte[] getPreviousEntryHash() {
    return this.previousEntryHash == null
        ? null
        : Arrays.copyOf(this.previousEntryHash, this.previousEntryHash.length);
  }

  @JsonProperty("signatures")
  public Map<String, byte[]> getSignatures() {
    return this.signatures.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> Arrays.copyOf(e.getValue(), e.getValue().length)
            )
        );
  }


  public static RootTrustBase fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, RootTrustBase.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(RootTrustBase.class, e);
    }
  }

  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(RootTrustBase.class, e);
    }
  }


  public static class NodeInfo {
    private final String nodeId;
    private final byte[] signingKey;
    private final long stakedAmount;

    @JsonCreator
    NodeInfo(
        @JsonProperty("nodeId") String nodeId,
        @JsonProperty("sigKey") byte[] signingKey,
        @JsonProperty("stake") long stakedAmount
    ) {
      this.nodeId = nodeId;
      this.signingKey = Arrays.copyOf(signingKey, signingKey.length);
      this.stakedAmount = stakedAmount;
    }

    @JsonProperty("nodeId")
    public String getNodeId() {
      return this.nodeId;
    }

    @JsonProperty("sigKey")
    public byte[] getSigningKey() {
      return Arrays.copyOf(this.signingKey, this.signingKey.length);
    }

    @JsonProperty("stake")
    public long getStakedAmount() {
      return this.stakedAmount;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof NodeInfo)) {
        return false;
      }
      NodeInfo nodeInfo = (NodeInfo) o;
      return Objects.equals(this.nodeId, nodeInfo.nodeId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(nodeId);
    }
  }
}