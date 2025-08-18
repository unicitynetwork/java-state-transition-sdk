
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.mtree.plain.SparseMerkleTreePath;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePath;
import java.util.Objects;

public class SplitMintReasonProof {
  private final SparseMerkleTreePath aggregationPath;
  private final SparseMerkleSumTreePath coinTreePath;

  public SplitMintReasonProof(
      SparseMerkleTreePath aggregationPath, SparseMerkleSumTreePath coinTreePath) {
    Objects.requireNonNull(aggregationPath, "aggregationPath cannot be null");
    Objects.requireNonNull(coinTreePath, "coinTreePath cannot be null");

    this.aggregationPath = aggregationPath;
    this.coinTreePath = coinTreePath;
  }

  public SparseMerkleTreePath getAggregationPath() {
    return this.aggregationPath;
  }

  public SparseMerkleSumTreePath getCoinTreePath() {
    return this.coinTreePath;
  }
}
