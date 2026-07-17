package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;

import java.util.Arrays;
import java.util.Objects;

class PendingLeafBranch implements LeafBranch {
  private final byte[] key;
  private final byte[] value;
  private final int depth;

  public PendingLeafBranch(byte[] key, byte[] value) {
    this.key = key;
    this.value = value;
    this.depth = key.length * 8;
  }

  @Override
  public int getDepth() {
    return this.depth;
  }

  @Override
  public int calculateSplitDepth(byte[] key) {
    return SparseMerkleTreePathUtils.commonPrefixLength(key, this.key, this.depth);
  }

  @Override
  public byte[] getKey() {
    return Arrays.copyOf(this.key, this.key.length);
  }

  @Override
  public byte[] getValue() {
    return Arrays.copyOf(this.value, this.value.length);
  }

  @Override
  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return FinalizedLeafBranch.fromPendingLeaf(hashAlgorithm, this);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PendingLeafBranch)) {
      return false;
    }

    PendingLeafBranch that = (PendingLeafBranch) o;
    return Arrays.equals(this.key, that.key) && Arrays.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.key), Arrays.hashCode(this.value));
  }
}
