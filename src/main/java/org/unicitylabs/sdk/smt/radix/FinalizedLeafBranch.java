package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;

import java.util.Arrays;
import java.util.Objects;

class FinalizedLeafBranch implements LeafBranch, FinalizedBranch {

  private final byte[] key;
  private final byte[] value;
  private final int depth;
  private final DataHash hash;

  private FinalizedLeafBranch(byte[] key, byte[] value, DataHash hash) {
    this.key = key;
    this.value = value;
    this.depth = key.length * 8;
    this.hash = hash;
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
  public DataHash getHash() {
    return this.hash;
  }

  @Override
  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FinalizedLeafBranch)) {
      return false;
    }
    FinalizedLeafBranch that = (FinalizedLeafBranch) o;
    return Arrays.equals(this.key, that.key) && Arrays.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.key), Arrays.hashCode(this.value));
  }

  public static FinalizedLeafBranch fromPendingLeaf(
          HashAlgorithm hashAlgorithm,
          PendingLeafBranch leaf
  ) {
    byte[] key = leaf.getKey();
    byte[] value = leaf.getValue();


    DataHash hash = new DataHasher(hashAlgorithm)
            .update(new byte[]{0x00})
            .update(key)
            .update(value)
            .digest();

    return new FinalizedLeafBranch(key, value, hash);
  }
}
