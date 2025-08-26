package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.hash.DataHash;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

public class SparseMerkleTreePathStep {

  private final BigInteger path;
  private final DataHash sibling;
  private final SparseMerkleTreePathStepBranch branch;

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedLeafBranch branch) {
    this(path, sibling, branch == null ? null : branch.getValue());
  }

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedNodeBranch branch) {
    this(path, sibling, branch == null ? null : branch.getChildrenHash().getData());
  }

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling, byte[] value) {
    this(
        path,
        sibling != null ? sibling.getHash() : null,
        value != null ? new SparseMerkleTreePathStepBranch(value) : null
    );
  }

  public SparseMerkleTreePathStep(BigInteger path, DataHash sibling,
      SparseMerkleTreePathStepBranch branch) {
    Objects.requireNonNull(path, "path cannot be null");

    this.path = path;
    this.sibling = sibling;
    this.branch = branch;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public Optional<DataHash> getSibling() {
    return Optional.ofNullable(this.sibling);
  }

  public Optional<SparseMerkleTreePathStepBranch> getBranch() {
    return Optional.ofNullable(this.branch);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleTreePathStep)) {
      return false;
    }
    SparseMerkleTreePathStep that = (SparseMerkleTreePathStep) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.sibling, that.sibling)
        && Objects.equals(this.branch, that.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.sibling, this.branch);
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePathStep{path=%s, sibling=%s, branch=%s}",
        this.path.toString(2), this.sibling, this.branch);
  }
}
