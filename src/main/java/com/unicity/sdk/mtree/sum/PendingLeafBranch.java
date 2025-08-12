package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

class PendingLeafBranch implements LeafBranch {

  private final BigInteger path;
  private final LeafValue value;

  public PendingLeafBranch(BigInteger path, LeafValue value) {
    this.path = path;
    this.value = value;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public LeafValue getValue() {
    return this.value;
  }

  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return FinalizedLeafBranch.create(this.path, this.value, hashAlgorithm);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PendingLeafBranch)) {
      return false;
    }
    PendingLeafBranch that = (PendingLeafBranch) o;
    return Objects.equals(this.path, that.path) && Objects.deepEquals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.value);
  }
}
