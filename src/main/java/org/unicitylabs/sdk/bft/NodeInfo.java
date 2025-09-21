package org.unicitylabs.sdk.bft;

import java.util.Arrays;

public class NodeInfo {

  public final String nodeId;
  private final byte[] signingKey;
  public final long stakedAmount;

  public NodeInfo(String nodeId, byte[] signingKey, long stakedAmount) {
    this.nodeId = nodeId;
    this.signingKey = Arrays.copyOf(signingKey, signingKey.length);
    this.stakedAmount = stakedAmount;
  }

  public byte[] getSigningKey() {
    return Arrays.copyOf(this.signingKey, this.signingKey.length);
  }
}