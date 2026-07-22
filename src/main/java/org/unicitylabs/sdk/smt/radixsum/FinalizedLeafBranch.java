package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;
import org.unicitylabs.sdk.util.BigIntegerConverter;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Finalized leaf in a radix sparse Merkle sum tree. The leaf hash is
 * {@code SHA-256(0x10 || key || data || u256(value))}, where {@code u256} is the 32-byte
 * big-endian encoding of the leaf amount.
 */
class FinalizedLeafBranch implements LeafBranch, FinalizedBranch {

  private final byte[] key;
  private final byte[] data;
  private final BigInteger value;
  private final int depth;
  private final DataHash hash;

  private FinalizedLeafBranch(byte[] key, byte[] data, BigInteger value, DataHash hash) {
    this.key = key;
    this.data = data;
    this.value = value;
    this.depth = key.length * 8;
    this.hash = hash;
  }

  public static FinalizedLeafBranch fromPendingLeaf(HashAlgorithm hashAlgorithm, PendingLeafBranch leaf) {
    byte[] key = leaf.getKey();
    byte[] data = leaf.getData();

    DataHash hash = new DataHasher(hashAlgorithm)
            .update(new byte[]{0x10})
            .update(key)
            .update(data)
            .update(BigIntegerConverter.encode(leaf.getValue(), 32))
            .digest();

    return new FinalizedLeafBranch(key, data, leaf.getValue(), hash);
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
  public DataHash getHash() {
    return this.hash;
  }

  @Override
  public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
    return this;
  }
}
