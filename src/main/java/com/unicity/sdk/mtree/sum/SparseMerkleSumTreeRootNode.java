package com.unicity.sdk.mtree.sum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.CommonPath;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePath.Root;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SparseMerkleSumTreeRootNode {

  private final BigInteger path = BigInteger.ONE; // Root path is always 0
  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final Root root;

  private SparseMerkleSumTreeRootNode(
      FinalizedBranch left, FinalizedBranch right, Root root) {
    this.left = left;
    this.right = right;
    this.root = root;
  }

  static SparseMerkleSumTreeRootNode create(
      FinalizedBranch left, FinalizedBranch right,
      HashAlgorithm hashAlgorithm) {
    try {
      DataHash rootHash = new DataHasher(hashAlgorithm)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
              UnicityObjectMapper.CBOR.createArrayNode()
                  .add(
                      left == null
                          ? null
                          : UnicityObjectMapper.CBOR.createArrayNode()
                              .add(left.getHash().getImprint())
                              .add(BigIntegerConverter.encode(left.getCounter()))
                  )
                  .add(
                      right == null
                          ? null
                          : UnicityObjectMapper.CBOR.createArrayNode()
                              .add(right.getHash().getImprint())
                              .add(BigIntegerConverter.encode(right.getCounter()))
                  )
          ))
          .digest();

      BigInteger counter = BigInteger.ZERO
          .add(left == null ? BigInteger.ZERO : left.getCounter())
          .add(right == null ? BigInteger.ZERO : right.getCounter());
      Root root = new Root(rootHash, counter);

      return new SparseMerkleSumTreeRootNode(left, right, root);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  public Root getRoot() {
    return this.root;
  }

  public SparseMerkleSumTreePath getPath(BigInteger path) {
    return new SparseMerkleSumTreePath(
        this.root,
        SparseMerkleSumTreeRootNode.generatePath(path, this.left, this.right)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleSumTreeRootNode)) {
      return false;
    }
    SparseMerkleSumTreeRootNode that = (SparseMerkleSumTreeRootNode) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.left, that.left)
        && Objects.equals(this.right, that.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.left, this.right);
  }

  private static List<SparseMerkleSumTreePathStep> generatePath(
      BigInteger remainingPath,
      FinalizedBranch left,
      FinalizedBranch right
  ) {
    boolean isRight = remainingPath.testBit(0);
    FinalizedBranch branch = isRight ? right : left;
    FinalizedBranch siblingBranch = isRight ? left : right;

    if (branch == null) {
      return List.of(new SparseMerkleSumTreePathStep(remainingPath, siblingBranch));
    }

    CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
    if (branch.getPath().equals(commonPath.getPath())) {
      if (branch instanceof FinalizedLeafBranch) {
        return List.of(
            new SparseMerkleSumTreePathStep(
                branch.getPath(),
                siblingBranch,
                (FinalizedLeafBranch) branch));
      }

      FinalizedNodeBranch nodeBranch = (FinalizedNodeBranch) branch;

      if (remainingPath.shiftRight(commonPath.getLength()).compareTo(BigInteger.ONE) == 0) {
        return List.of(
            new SparseMerkleSumTreePathStep(branch.getPath(), siblingBranch, nodeBranch));
      }

      return Stream.concat(
              SparseMerkleSumTreeRootNode.generatePath(
                      remainingPath.shiftRight(commonPath.getLength()),
                      nodeBranch.getLeft(),
                      nodeBranch.getRight()
                  )
                  .stream(),
              Stream.of(
                  new SparseMerkleSumTreePathStep(branch.getPath(), siblingBranch, nodeBranch))
          )
          .collect(Collectors.toUnmodifiableList());
    }

    if (branch instanceof FinalizedLeafBranch) {
      return List.of(
          new SparseMerkleSumTreePathStep(branch.getPath(), siblingBranch,
              (FinalizedLeafBranch) branch));
    }

    return List.of(
        new SparseMerkleSumTreePathStep(branch.getPath(), siblingBranch,
            (FinalizedNodeBranch) branch));
  }
}
