
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.mtree.plain.MerkleTreePath;
import java.util.Objects;

public class SplitMintReasonProof {
  private final MerkleTreePath aggregationPath;
  private final MerkleTreePath coinTreePath;

  public SplitMintReasonProof(MerkleTreePath aggregationPath, MerkleTreePath coinTreePath) {
    Objects.requireNonNull(aggregationPath, "aggregationPath cannot be null");
    Objects.requireNonNull(coinTreePath, "coinTreePath cannot be null");

    this.aggregationPath = aggregationPath;
    this.coinTreePath = coinTreePath;
  }

  public MerkleTreePath getAggregationPath() {
    return aggregationPath;
  }

  public MerkleTreePath getCoinTreePath() {
    return coinTreePath;
  }
}
