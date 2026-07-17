package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;
import org.unicitylabs.sdk.util.LongConverter;

import java.util.Arrays;

class FinalizedNodeBranch implements NodeBranch, FinalizedBranch {
  private final byte[] path;
  private final int depth;
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final DataHash hash;

  private FinalizedNodeBranch(
          byte[] path,
          int depth,
          FinalizedBranch left,
          FinalizedBranch right,
          DataHash hash
  ) {
    this.path = path;
    this.depth = depth;
    this.left = left;
    this.right = right;
    this.hash = hash;
  }

  @Override
  public byte[] getPath() {
    return Arrays.copyOf(this.path, this.path.length);
  }

  @Override
  public int getDepth() {
    return this.depth;
  }

  @Override
  public int calculateSplitDepth(byte[] key) {
    return SparseMerkleTreePathUtils.commonPrefixLength(key, this.path, this.depth);
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
  public DataHash getHash() {
    return this.hash;
  }

  public static FinalizedNodeBranch fromPendingNode(HashAlgorithm hashAlgorithm, PendingNodeBranch node) {
    FinalizedBranch left = node.getLeft() != null ? node.getLeft().finalize(hashAlgorithm) : null;
    FinalizedBranch right = node.getRight() != null ? node.getRight().finalize(hashAlgorithm) : null;

    if (left == null && right == null) {
      return new FinalizedNodeBranch(node.getPath(), node.getDepth(), left, right, new DataHash(HashAlgorithm.SHA256, new byte[32]));
    }

    if (left != null && right == null) {
      return new FinalizedNodeBranch(node.getPath(), node.getDepth(), left, right, left.getHash());
    }

    if (left == null) {
      return new FinalizedNodeBranch(node.getPath(), node.getDepth(), left, right, right.getHash());
    }

    DataHash hash = new DataHasher(hashAlgorithm)
            .update(new byte[]{0x01})
            .update(LongConverter.encode(node.getDepth()))
            .update(node.getPath())
            .update(left.getHash().getData())
            .update(right.getHash().getData())
            .digest();

    return new FinalizedNodeBranch(node.getPath(), node.getDepth(), left, right, hash);
  }

  @Override
  public PendingNodeBranch withLeftBranch(Branch left) {
    return new PendingNodeBranch(this.path, this.depth, left, this.right);
  }

  @Override
  public PendingNodeBranch withRightBranch(Branch right) {
    return new PendingNodeBranch(this.path, this.depth, this.left, right);
  }

  @Override
  public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) {
    return this;
  }
}
