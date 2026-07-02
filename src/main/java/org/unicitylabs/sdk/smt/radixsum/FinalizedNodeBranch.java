package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.BigIntegerConverter;

import java.math.BigInteger;

/**
 * Finalized interior node in a radix sparse Merkle sum tree. The node hash is
 * {@code SHA-256(0x11 || u8(depth) || hL || u256(vL) || hR || u256(vR))} and the node sum is
 * {@code vL + vR}, computed with a checked 256-bit addition.
 */
public class FinalizedNodeBranch implements NodeBranch, FinalizedBranch {

  private static final BigInteger SUM_LIMIT = BigInteger.ONE.shiftLeft(256);

  private final BigInteger path;
  private final int depth;
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final BigInteger value;
  private final DataHash hash;

  private FinalizedNodeBranch(BigInteger path, int depth, FinalizedBranch left,
                              FinalizedBranch right, BigInteger value, DataHash hash) {
    this.path = path;
    this.depth = depth;
    this.left = left;
    this.right = right;
    this.value = value;
    this.hash = hash;
  }

  static FinalizedNodeBranch fromPendingNode(HashAlgorithm hashAlgorithm, PendingNodeBranch node) {
    FinalizedBranch left = node.getLeft().finalize(hashAlgorithm);
    FinalizedBranch right = node.getRight().finalize(hashAlgorithm);

    BigInteger value = left.getValue().add(right.getValue());
    if (value.compareTo(FinalizedNodeBranch.SUM_LIMIT) >= 0) {
      throw new ArithmeticException("RSMST internal sum overflow.");
    }

    DataHash hash = new DataHasher(hashAlgorithm)
            .update(new byte[]{0x11, (byte) node.getDepth()})
            .update(left.getHash().getData())
            .update(BigIntegerConverter.encode(left.getValue(), 32))
            .update(right.getHash().getData())
            .update(BigIntegerConverter.encode(right.getValue(), 32))
            .digest();

    return new FinalizedNodeBranch(node.getPath(), node.getDepth(), left, right, value, hash);
  }

  @Override
  public BigInteger getPath() {
    return this.path;
  }

  @Override
  public int getDepth() {
    return this.depth;
  }

  @Override
  public FinalizedBranch getLeft() {
    return this.left;
  }

  @Override
  public FinalizedBranch getRight() {
    return this.right;
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
  public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) {
    return this;
  }
}
