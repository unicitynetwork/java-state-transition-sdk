package org.unicitylabs.sdk.api.bft;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.serializer.json.LongAsStringSerializer;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Root trust base information.
 */
public class RootTrustBase {

  private static final long VERSION = 1;

  private final NetworkId networkId;
  private final long epoch;
  private final long epochStartRound;
  private final Map<String, NodeInfo> rootNodes;
  private final long quorumThreshold;
  private final byte[] stateHash;
  private final byte[] changeRecordHash;
  private final byte[] previousEntryHash;
  private final Map<String, byte[]> signatures;

  @JsonCreator
  RootTrustBase(
          @JsonProperty("version") long version,
          @JsonProperty("networkId") NetworkId networkId,
          @JsonProperty("epoch") long epoch,
          @JsonProperty("epochStartRound") long epochStartRound,
          @JsonProperty("rootNodes") List<NodeInfo> rootNodes,
          @JsonProperty("quorumThreshold") long quorumThreshold,
          @JsonProperty("stateHash") byte[] stateHash,
          @JsonProperty("changeRecordHash") byte[] changeRecordHash,
          @JsonProperty("previousEntryHash") byte[] previousEntryHash,
          @JsonProperty("signatures") Map<String, byte[]> signatures
  ) {
    if (version != RootTrustBase.VERSION) {
      throw new IllegalArgumentException(
              String.format("Unsupported RootTrustBase version: %s", version));
    }

    Map<String, NodeInfo> nodes = new LinkedHashMap<>();
    Set<String> signingKeys = new HashSet<>();
    for (NodeInfo node : rootNodes) {
      if (nodes.putIfAbsent(node.getNodeId(), node) != null) {
        throw new IllegalArgumentException(
                String.format("Duplicate trust base node id: %s", node.getNodeId()));
      }
      if (!signingKeys.add(HexConverter.encode(node.getSigningKey()))) {
        throw new IllegalArgumentException("Duplicate trust base signing key.");
      }
    }

    if (nodes.isEmpty()) {
      throw new IllegalArgumentException("Trust base must contain at least one root node.");
    }

    if (quorumThreshold < 1 || quorumThreshold > nodes.size()) {
      throw new IllegalArgumentException(
              "Trust base quorum threshold must be between 1 and the root node count.");
    }

    this.networkId = networkId;
    this.epoch = epoch;
    this.epochStartRound = epochStartRound;
    this.rootNodes = nodes;
    this.quorumThreshold = quorumThreshold;
    this.stateHash = Arrays.copyOf(stateHash, stateHash.length);
    this.changeRecordHash = changeRecordHash == null
            ? null
            : Arrays.copyOf(changeRecordHash, changeRecordHash.length);
    this.previousEntryHash = previousEntryHash == null
            ? null
            : Arrays.copyOf(previousEntryHash, previousEntryHash.length);
    this.signatures = signatures.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Arrays.copyOf(e.getValue(), e.getValue().length)
            ));
  }

  /**
   * Get version.
   *
   * @return version
   */
  @JsonSerialize(using = LongAsStringSerializer.class)
  public long getVersion() {
    return RootTrustBase.VERSION;
  }

  /**
   * Get network id.
   *
   * @return network id
   */
  public NetworkId getNetworkId() {
    return this.networkId;
  }

  /**
   * Get current epoch.
   *
   * @return epoch
   */
  @JsonSerialize(using = LongAsStringSerializer.class)
  public long getEpoch() {
    return this.epoch;
  }

  /**
   * Get epoch start round.
   *
   * @return epoch start round
   */
  @JsonSerialize(using = LongAsStringSerializer.class)
  public long getEpochStartRound() {
    return this.epochStartRound;
  }

  /**
   * Get root nodes.
   *
   * @return root nodes
   */
  public Set<NodeInfo> getRootNodes() {
    return Set.copyOf(this.rootNodes.values());
  }

  /**
   * Get root node by node id.
   *
   * @param nodeId node id
   * @return node info, or {@code null} when no node with the given id exists
   */
  public NodeInfo getRootNode(String nodeId) {
    return this.rootNodes.get(nodeId);
  }

  /**
   * Get quorum threshold.
   *
   * @return quorum threshold
   */
  @JsonSerialize(using = LongAsStringSerializer.class)
  public long getQuorumThreshold() {
    return this.quorumThreshold;
  }

  /**
   * Get state hash.
   *
   * @return state hash
   */
  public byte[] getStateHash() {
    return Arrays.copyOf(this.stateHash, this.stateHash.length);
  }

  /**
   * Get change record hash.
   *
   * @return change record hash
   */
  public byte[] getChangeRecordHash() {
    return this.changeRecordHash == null
            ? null
            : Arrays.copyOf(this.changeRecordHash, this.changeRecordHash.length);
  }

  /**
   * Get previous entry hash.
   *
   * @return previous entry hash
   */
  public byte[] getPreviousEntryHash() {
    return this.previousEntryHash == null
            ? null
            : Arrays.copyOf(this.previousEntryHash, this.previousEntryHash.length);
  }

  /**
   * Get signatures.
   *
   * @return signatures
   */
  public Map<String, byte[]> getSignatures() {
    return Map.copyOf(
            this.signatures.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> Arrays.copyOf(e.getValue(), e.getValue().length)
                    ))
    );
  }

  /**
   * Create a root trust base from JSON string.
   *
   * @param input JSON string
   * @return root trust base
   */
  public static RootTrustBase fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, RootTrustBase.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(RootTrustBase.class, e);
    }
  }

  /**
   * Convert root trust base to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(RootTrustBase.class, e);
    }
  }

  /**
   * Node information.
   */
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
      if (stakedAmount <= 0) {
        throw new IllegalArgumentException("Each trust base root node must have positive stake.");
      }

      this.nodeId = nodeId;
      this.signingKey = Arrays.copyOf(signingKey, signingKey.length);
      this.stakedAmount = stakedAmount;
    }

    /**
     * Get node ID.
     *
     * @return node ID
     */
    public String getNodeId() {
      return this.nodeId;
    }

    /**
     * Get signing key.
     *
     * @return signing key
     */
    @JsonProperty("sigKey")
    public byte[] getSigningKey() {
      return Arrays.copyOf(this.signingKey, this.signingKey.length);
    }

    /**
     * Get staked amount.
     *
     * @return staked amount
     */
    @JsonProperty("stake")
    @JsonSerialize(using = LongAsStringSerializer.class)
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