package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.util.HexConverter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class SparseMerkleTreePathStep {

  private final BigInteger path;
  private final Branch sibling;
  private final Branch branch;

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling,
      FinalizedLeafBranch branch) {
    this(
        path,
        sibling,
        branch == null
            ? null
            : new Branch(branch.getValue())
    );
  }

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling,
      FinalizedNodeBranch branch) {
    this(
        path,
        sibling,
        branch == null ? null
            : new Branch(branch.getChildrenHash().getData())
    );
  }

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling, Branch branch) {
    this(
        path,
        sibling == null ? null : new Branch(sibling.getHash().getData()),
        branch
    );
  }

  public SparseMerkleTreePathStep(BigInteger path, Branch sibling, Branch branch) {
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

  public static class Branch {

    private final byte[] value;

    public Branch(byte[] value) {
      this.value = value == null ? null : Arrays.copyOf(value, value.length);
    }

    public byte[] getValue() {
      return this.value == null ? null : Arrays.copyOf(this.value, this.value.length);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Branch)) {
        return false;
      }
      Branch branch = (Branch) o;
      return Objects.deepEquals(this.value, branch.value);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(this.value);
    }

    @Override
    public String toString() {
      return String.format(
          "Branch{value=%s}",
          this.value == null ? null : HexConverter.encode(this.value)
      );
    }
  }
}
