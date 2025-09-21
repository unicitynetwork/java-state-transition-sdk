package org.unicitylabs.sdk.bft;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RootTrustBase {
  public final long version;
  public final long epoch;
  public final long epochStartRound;
  public final List<NodeInfo> rootNodes;
  public final long quorumThreshold;
  private final byte[] stateHash;
  private final byte[] changeRecordHash;
  private final byte[] previousEntryHash;
  private final Map<String, byte[]> signatures;

  public RootTrustBase(
      long version,
      long epoch,
      long epochStartRound,
      List<NodeInfo> rootNodes,
      long quorumThreshold,
      byte[] stateHash,
      byte[] changeRecordHash,
      byte[] previousEntryHash,
      Map<String, byte[]> signatures
  ) {
    this.version = version;
    this.epoch = epoch;
    this.epochStartRound = epochStartRound;
    this.rootNodes = Collections.unmodifiableList(rootNodes);
    this.quorumThreshold = quorumThreshold;
    this.stateHash = Arrays.copyOf(stateHash, stateHash.length);
    this.changeRecordHash = Arrays.copyOf(changeRecordHash, changeRecordHash.length);
    this.previousEntryHash = Arrays.copyOf(previousEntryHash, previousEntryHash.length);
    this.signatures = signatures.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                Map.Entry::getKey,
                e -> Arrays.copyOf(e.getValue(), e.getValue().length)
            )
        );
  }

  public byte[] getStateHash() {
    return Arrays.copyOf(this.stateHash, this.stateHash.length);
  }

  public byte[] getChangeRecordHash() {
    return Arrays.copyOf(this.changeRecordHash, this.changeRecordHash.length);
  }

  public byte[] getPreviousEntryHash() {
    return Arrays.copyOf(this.previousEntryHash, this.previousEntryHash.length);
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
}