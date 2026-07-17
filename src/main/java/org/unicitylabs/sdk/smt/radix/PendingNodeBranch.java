package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;

import java.util.Arrays;

class PendingNodeBranch implements NodeBranch {
  private final byte[] path;
  private final int depth;
  private final Branch left;
  private final Branch right;

  public PendingNodeBranch(byte[] path, int depth, Branch left, Branch right) {
    this.path = path;
    this.depth = depth;
    this.left = left;
    this.right = right;
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
  public Branch getLeft() {
    return this.left;
  }

  @Override
  public Branch getRight() {
    return this.right;
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
    return FinalizedNodeBranch.fromPendingNode(
            hashAlgorithm,
            this
    );
  }
}
