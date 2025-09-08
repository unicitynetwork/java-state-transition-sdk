
package org.unicitylabs.sdk.transaction.split;

import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePath;
import org.unicitylabs.sdk.token.fungible.CoinId;
import java.util.Objects;

public class SplitMintReasonProof {

  private final CoinId coinId;
  private final SparseMerkleTreePath aggregationPath;
  private final SparseMerkleSumTreePath coinTreePath;

  public SplitMintReasonProof(
      CoinId coinId,
      SparseMerkleTreePath aggregationPath, SparseMerkleSumTreePath coinTreePath) {
    Objects.requireNonNull(coinId, "coinId cannot be null");
    Objects.requireNonNull(aggregationPath, "aggregationPath cannot be null");
    Objects.requireNonNull(coinTreePath, "coinTreePath cannot be null");

    this.coinId = coinId;
    this.aggregationPath = aggregationPath;
    this.coinTreePath = coinTreePath;
  }

  public CoinId getCoinId() {
    return this.coinId;
  }

  public SparseMerkleTreePath getAggregationPath() {
    return this.aggregationPath;
  }

  public SparseMerkleSumTreePath getCoinTreePath() {
    return this.coinTreePath;
  }
}
