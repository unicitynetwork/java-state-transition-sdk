package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Pending leaf in a radix sparse Merkle sum tree, awaiting hashing.
 */
public class PendingLeafBranch implements LeafBranch {
  private final BigInteger path;
  private final byte[] key;
  private final byte[] data;
  private final BigInteger value;

  PendingLeafBranch(BigInteger path, byte[] key, byte[] data, BigInteger value) {
    this.path = path;
    this.key = Arrays.copyOf(key, key.length);
    this.data = Arrays.copyOf(data, data.length);
    this.value = value;
  }

  @Override
  public BigInteger getPath() {
    return this.path;
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
