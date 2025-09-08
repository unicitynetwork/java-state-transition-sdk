package org.unicitylabs.sdk.mtree.plain;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.CommonPath;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparseMerkleTreeRootNode {

  private final BigInteger path = BigInteger.ONE; // Root path is always 0
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final DataHash rootHash;

  private SparseMerkleTreeRootNode(
      FinalizedBranch left, FinalizedBranch right, DataHash rootHash) {
    this.left = left;
    this.right = right;
    this.rootHash = rootHash;
  }

  static SparseMerkleTreeRootNode create(
      FinalizedBranch left, FinalizedBranch right,
      HashAlgorithm hashAlgorithm) {
    DataHash rootHash = new DataHasher(hashAlgorithm)
        .update(left == null ? new byte[]{0} : left.getHash().getData())
        .update(right == null ? new byte[]{0} : right.getHash().getData())
        .digest();
    return new SparseMerkleTreeRootNode(left, right, rootHash);
  }

  public DataHash getRootHash() {
    return this.rootHash;
  }

  public SparseMerkleTreePath getPath(BigInteger path) {
    return new SparseMerkleTreePath(this.rootHash,
        SparseMerkleTreeRootNode.generatePath(path, this.left, this.right));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleTreeRootNode)) {
      return false;
    }
    SparseMerkleTreeRootNode that = (SparseMerkleTreeRootNode) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.left, that.left)
        && Objects.equals(this.right, that.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.left, this.right);
  }

  private static List<SparseMerkleTreePathStep> generatePath(
      BigInteger remainingPath,
      FinalizedBranch left,
      FinalizedBranch right
  ) {
    boolean isRight = remainingPath.testBit(0);
    FinalizedBranch branch = isRight ? right : left;
    FinalizedBranch siblingBranch = isRight ? left : right;

    if (branch == null) {
      return List.of(
          new SparseMerkleTreePathStep(remainingPath, siblingBranch, (FinalizedLeafBranch) null));
    }

    CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
    if (branch.getPath().equals(commonPath.getPath())) {
      if (branch instanceof FinalizedLeafBranch) {
        return List.of(
            new SparseMerkleTreePathStep(branch.getPath(), siblingBranch,
                (FinalizedLeafBranch) branch));
      }

      FinalizedNodeBranch nodeBranch = (FinalizedNodeBranch) branch;

      if (remainingPath.shiftRight(commonPath.getLength()).compareTo(BigInteger.ONE) == 0) {
        return List.of(new SparseMerkleTreePathStep(branch.getPath(), siblingBranch, nodeBranch));
      }

      return List.copyOf(
          Stream.concat(
                  SparseMerkleTreeRootNode.generatePath(
                      remainingPath.shiftRight(commonPath.getLength()),
                      nodeBranch.getLeft(),
                      nodeBranch.getRight()
                  ).stream(),
                  Stream.of(
                      new SparseMerkleTreePathStep(branch.getPath(), siblingBranch, nodeBranch)
                  )
              )
              .collect(Collectors.toList())
      );
    }

    if (branch instanceof FinalizedLeafBranch) {
      return List.of(
          new SparseMerkleTreePathStep(branch.getPath(), siblingBranch,
              (FinalizedLeafBranch) branch));
    }

    return List.of(
        new SparseMerkleTreePathStep(branch.getPath(), siblingBranch,
            (FinalizedNodeBranch) branch));
  }
}
