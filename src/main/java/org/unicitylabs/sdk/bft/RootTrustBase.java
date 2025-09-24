package org.unicitylabs.sdk.bft;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

  public RootTrustBase(
      long version,
      int networkId,
      long epoch,
      long epochStartRound,
      Set<NodeInfo> rootNodes,
      long quorumThreshold,
      byte[] stateHash,
      byte[] changeRecordHash,
      byte[] previousEntryHash,
      Map<String, byte[]> signatures
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

  public long getVersion() {
    return this.version;
  }

  public int getNetworkId() {
    return this.networkId;
  }

  public long getEpoch() {
    return this.epoch;
  }

  public long getEpochStartRound() {
    return this.epochStartRound;
  }

  public Set<NodeInfo> getRootNodes() {
    return this.rootNodes;
  }

  public long getQuorumThreshold() {
    return this.quorumThreshold;
  }

  public byte[] getStateHash() {
    return Arrays.copyOf(this.stateHash, this.stateHash.length);
  }

  public byte[] getChangeRecordHash() {
    return this.changeRecordHash == null
        ? null
        : Arrays.copyOf(this.changeRecordHash, this.changeRecordHash.length);
  }

  public byte[] getPreviousEntryHash() {
    return this.previousEntryHash == null
        ? null
        : Arrays.copyOf(this.previousEntryHash, this.previousEntryHash.length);
  }

  public Map<String, byte[]> getSignatures() {
    return this.signatures.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> Arrays.copyOf(e.getValue(), e.getValue().length)
            )
        );
  }

  public static class NodeInfo {

    private final String nodeId;
    private final byte[] signingKey;
    private final long stakedAmount;

    public NodeInfo(String nodeId, byte[] signingKey, long stakedAmount) {
      this.nodeId = nodeId;
      this.signingKey = Arrays.copyOf(signingKey, signingKey.length);
      this.stakedAmount = stakedAmount;
    }

    public String getNodeId() {
      return this.nodeId;
    }

    public byte[] getSigningKey() {
      return Arrays.copyOf(this.signingKey, this.signingKey.length);
    }

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