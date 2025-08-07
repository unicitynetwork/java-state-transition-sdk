package com.unicity.sdk.smt.path;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.smt.FinalizedBranch;
import com.unicity.sdk.smt.FinalizedLeafBranch;
import com.unicity.sdk.smt.FinalizedNodeBranch;

import java.math.BigInteger;
import java.util.Objects;

public class MerkleTreePathStep {

  private final BigInteger path;
  private final DataHash sibling;
  private final MerkleTreePathStepBranch branch;

  public MerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedLeafBranch branch) {
    this(path, sibling, branch == null ? null : branch.getValue());
  }

  public MerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedNodeBranch branch) {
    this(path, sibling, branch == null ? null : branch.getChildrenHash().getData());
  }

  public MerkleTreePathStep(BigInteger path, FinalizedBranch sibling, byte[] value) {
    this(
        path,
        sibling != null ? sibling.getHash() : null,
        value != null ? new MerkleTreePathStepBranch(value) : null
    );
  }

  public MerkleTreePathStep(BigInteger path, DataHash sibling, MerkleTreePathStepBranch branch) {
    Objects.requireNonNull(path, "path cannot be null");

    this.path = path;
    this.sibling = sibling;
    this.branch = branch;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public DataHash getSibling() {
    return this.sibling;
  }

  public MerkleTreePathStepBranch getBranch() {
    return this.branch;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MerkleTreePathStep)) {
      return false;
    }
    MerkleTreePathStep that = (MerkleTreePathStep) o;
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
