package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Pending leaf in a radix sparse Merkle sum tree, awaiting hashing.
 */
class PendingLeafBranch implements LeafBranch {
  private final byte[] key;
  private final byte[] data;
  private final BigInteger value;
  private final int depth;

  public PendingLeafBranch(byte[] key, byte[] data, BigInteger value) {
    this.key = key;
    this.data = data;
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
  public byte[] getData() {
    return Arrays.copyOf(this.data, this.data.length);
  }

  @Override
  public BigInteger getValue() {
    return this.value;
  }

  @Override
  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return FinalizedLeafBranch.fromPendingLeaf(hashAlgorithm, this);
  }
}
