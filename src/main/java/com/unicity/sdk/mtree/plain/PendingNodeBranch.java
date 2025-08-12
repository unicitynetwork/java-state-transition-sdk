package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.hash.HashAlgorithm;
import java.math.BigInteger;
import java.util.Objects;

class PendingNodeBranch implements NodeBranch {

  private final BigInteger path;
  private final Branch left;
  private final Branch right;

  public PendingNodeBranch(BigInteger path, Branch left, Branch right) {
    this.path = path;
    this.left = left;
    this.right = right;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public Branch getLeft() {
    return this.left;
  }

  public Branch getRight() {
    return this.right;
  }

  public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) {
    return FinalizedNodeBranch.create(this.path, this.left.finalize(hashAlgorithm),
        this.right.finalize(hashAlgorithm), hashAlgorithm);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PendingNodeBranch)) {
      return false;
    }
    PendingNodeBranch that = (PendingNodeBranch) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.left, that.left)
        && Objects.equals(this.right, that.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.left, this.right);
  }
}
