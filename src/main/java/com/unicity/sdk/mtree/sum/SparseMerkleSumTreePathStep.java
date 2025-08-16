package com.unicity.sdk.mtree.sum;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class SparseMerkleSumTreePathStep {

  private final BigInteger path;
  private final Branch sibling;
  private final Branch branch;

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedLeafBranch branch) {
    this(
        path,
        sibling,
        branch == null
            ? null
            : new Branch(branch.getValue().getValue(), branch.getValue().getCounter())
    );
  }

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedNodeBranch branch) {
    this(
        path,
        sibling,
        branch == null ? null
            : new Branch(branch.getChildrenHash().getImprint(), branch.getCounter())
    );
  }

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling) {
    this(
        path,
        sibling,
        (Branch) null
    );
  }

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling, Branch branch) {
    this(
        path,
        sibling == null ? null : new Branch(sibling.getHash().getImprint(), sibling.getCounter()),
        branch
    );
  }

  public SparseMerkleSumTreePathStep(BigInteger path, Branch sibling, Branch branch) {
    Objects.requireNonNull(path, "path cannot be null");

    this.path = path;
    this.sibling = sibling;
    this.branch = branch;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public Optional<Branch> getSibling() {
    return Optional.ofNullable(this.sibling);
  }

  public Optional<Branch> getBranch() {
    return Optional.ofNullable(this.branch);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleSumTreePathStep)) {
      return false;
    }
    SparseMerkleSumTreePathStep that = (SparseMerkleSumTreePathStep) o;
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

  public static class Branch {

    private final byte[] value;
    private final BigInteger counter;

    public Branch(byte[] value, BigInteger counter) {
      Objects.requireNonNull(value, "hash cannot be null");
      Objects.requireNonNull(counter, "counter cannot be null");

      this.value = Arrays.copyOf(value, value.length);
      this.counter = counter;
    }

    public byte[] getValue() {
      return Arrays.copyOf(this.value, this.value.length);
    }

    public BigInteger getCounter() {
      return this.counter;
    }
  }
}
