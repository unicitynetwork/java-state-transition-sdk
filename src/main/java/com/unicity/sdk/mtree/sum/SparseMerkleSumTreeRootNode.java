package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.CommonPath;
import com.unicity.sdk.mtree.sum.MerkleTreePath.Root;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparseMerkleSumTreeRootNode {

  private final BigInteger path = BigInteger.ONE; // Root path is always 0
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final DataHash rootHash;

  private SparseMerkleSumTreeRootNode(
      FinalizedBranch left, FinalizedBranch right, DataHash rootHash) {
    this.left = left;
    this.right = right;
    this.rootHash = rootHash;
  }

  static SparseMerkleSumTreeRootNode create(
      FinalizedBranch left, FinalizedBranch right,
      HashAlgorithm hashAlgorithm) {
    DataHash rootHash = new DataHasher(hashAlgorithm).update(
            left == null ? new byte[]{0} : left.getHash().getData())
        .update(right == null ? new byte[]{0} : right.getHash().getData()).digest();
    return new SparseMerkleSumTreeRootNode(left, right, rootHash);
  }

  public DataHash getRootHash() {
    return this.rootHash;
  }

  public MerkleTreePath getPath(BigInteger path) {
    return new MerkleTreePath(
        new Root(this.rootHash, BigInteger.ZERO),
        SparseMerkleSumTreeRootNode.generatePath(path, this.left, this.right));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleSumTreeRootNode)) {
      return false;
    }
    SparseMerkleSumTreeRootNode that = (SparseMerkleSumTreeRootNode) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.left, that.left) && Objects.equals(this.right, that.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.left, this.right);
  }

  private static List<MerkleTreePathStep> generatePath(
      BigInteger remainingPath,
      FinalizedBranch left,
      FinalizedBranch right
  ) {
    boolean isRight = remainingPath.testBit(0);
    FinalizedBranch branch = isRight ? right : left;
    FinalizedBranch siblingBranch = isRight ? left : right;

    if (branch == null) {
      return List.of(new MerkleTreePathStep(remainingPath, siblingBranch));
    }

    CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
    if (branch.getPath().equals(commonPath.getPath())) {
      if (branch instanceof FinalizedLeafBranch) {
        return List.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, (FinalizedLeafBranch) branch));
      }

      FinalizedNodeBranch nodeBranch = (FinalizedNodeBranch) branch;

      if (remainingPath.shiftRight(commonPath.getLength()).compareTo(BigInteger.ONE) == 0) {
        return List.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, nodeBranch));
      }

      return Stream.concat(
          SparseMerkleSumTreeRootNode.generatePath(remainingPath.shiftRight(commonPath.getLength()),
              nodeBranch.getLeft(), nodeBranch.getRight()).stream(),
          Stream.of(new MerkleTreePathStep(branch.getPath(), siblingBranch, nodeBranch))
      ).collect(Collectors.toUnmodifiableList());
    }

    if (branch instanceof FinalizedLeafBranch) {
      return List.of(
          new MerkleTreePathStep(branch.getPath(), siblingBranch, (FinalizedLeafBranch) branch));
    }

    return List.of(
        new MerkleTreePathStep(branch.getPath(), siblingBranch, (FinalizedNodeBranch) branch));
  }
}
