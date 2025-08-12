package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.BranchExistsException;
import com.unicity.sdk.mtree.CommonPath;
import com.unicity.sdk.mtree.LeafOutOfBoundsException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class SparseMerkleSumTree {

  private Branch left = null;
  private Branch right = null;

  private final HashAlgorithm hashAlgorithm;

  public SparseMerkleSumTree(HashAlgorithm hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  public synchronized void addLeaf(BigInteger path, LeafValue value) throws BranchExistsException, LeafOutOfBoundsException {
    if (path.compareTo(BigInteger.ONE) < 0) {
      throw new IllegalArgumentException("Path must be greater than 0");
    }

    boolean isRight = path.testBit(0);
    Branch branch = isRight ? right : left;
    Branch result = branch != null
        ? SparseMerkleSumTree.buildTree(branch, path, value)
        : new PendingLeafBranch(path, value);

    if (isRight) {
      this.right = result;
    } else {
      this.left = result;
    }
  }

  public synchronized SparseMerkleSumTreeRootNode calculateRoot() {
    FinalizedBranch left = this.left != null ? this.left.finalize(this.hashAlgorithm) : null;
    FinalizedBranch right = this.right != null ? this.right.finalize(this.hashAlgorithm) : null;
    this.left = left;
    this.right = right;

    return SparseMerkleSumTreeRootNode.create(left, right, this.hashAlgorithm);
  }

  private static Branch buildTree(Branch branch, BigInteger remainingPath, LeafValue value)
      throws BranchExistsException, LeafOutOfBoundsException {
    CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
    boolean isRight = remainingPath.shiftRight(commonPath.getLength()).testBit(0);

    if (commonPath.getPath().equals(remainingPath)) {
      throw new BranchExistsException();
    }

    if (branch instanceof LeafBranch) {
      if (commonPath.getPath().equals(branch.getPath())) {
        throw new LeafOutOfBoundsException();
      }

      LeafBranch leafBranch = (LeafBranch) branch;

      LeafBranch oldBranch = new PendingLeafBranch(
          branch.getPath().shiftRight(commonPath.getLength()), leafBranch.getValue());
      LeafBranch newBranch = new PendingLeafBranch(remainingPath.shiftRight(commonPath.getLength()),
          value);
      return new PendingNodeBranch(commonPath.getPath(), isRight ? oldBranch : newBranch,
          isRight ? newBranch : oldBranch);
    }

    NodeBranch nodeBranch = (NodeBranch) branch;

    // if node branch is split in the middle
    if (commonPath.getPath().compareTo(branch.getPath()) < 0) {
      LeafBranch newBranch = new PendingLeafBranch(remainingPath.shiftRight(commonPath.getLength()),
          value);
      NodeBranch oldBranch = new PendingNodeBranch(
          branch.getPath().shiftRight(commonPath.getLength()), nodeBranch.getLeft(),
          nodeBranch.getRight());
      return new PendingNodeBranch(commonPath.getPath(), isRight ? oldBranch : newBranch,
          isRight ? newBranch : oldBranch);
    }

    if (isRight) {
      return new PendingNodeBranch(nodeBranch.getPath(), nodeBranch.getLeft(),
          SparseMerkleSumTree.buildTree(nodeBranch.getRight(),
              remainingPath.shiftRight(commonPath.getLength()), value));
    }

    return new PendingNodeBranch(nodeBranch.getPath(),
        SparseMerkleSumTree.buildTree(nodeBranch.getLeft(),
            remainingPath.shiftRight(commonPath.getLength()), value), nodeBranch.getRight());
  }

  public static class LeafValue {
    private final byte[] value;
    private final BigInteger sum;

    public LeafValue(byte[] value, BigInteger sum) {
      this.value = Arrays.copyOf(value, value.length);
      this.sum = sum;
    }

    public byte[] getValue() {
      return Arrays.copyOf(this.value, this.value.length);
    }

    public BigInteger getCounter() {
      return this.sum;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof LeafValue)) {
        return false;
      }
      LeafValue that = (LeafValue) o;
      return Arrays.equals(this.value, that.value) && Objects.equals(this.sum, that.sum);
    }

    @Override
    public int hashCode() {
      return Objects.hash(Arrays.hashCode(this.value), this.sum);
    }
  }
}

