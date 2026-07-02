package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;

import java.math.BigInteger;

/**
 * Radix sparse Merkle sum tree root node. If the tree holds a single leaf the root hash and sum
 * are that leaf's; otherwise the root is the top internal node, which bifurcates at depth 0.
 */
public class SparseMerkleSumTreeRootNode {

  private final FinalizedBranch left;
  private final FinalizedBranch right;
  private final BigInteger value;
  private final DataHash hash;

  private SparseMerkleSumTreeRootNode(FinalizedBranch left, FinalizedBranch right,
                                      BigInteger value, DataHash hash) {
    this.left = left;
    this.right = right;
    this.value = value;
    this.hash = hash;
  }

  static SparseMerkleSumTreeRootNode create(FinalizedBranch left, FinalizedBranch right,
                                            HashAlgorithm hashAlgorithm) {
    if (left != null && right == null) {
      return new SparseMerkleSumTreeRootNode(left, null, left.getValue(), left.getHash());
    }
    if (left == null && right != null) {
      return new SparseMerkleSumTreeRootNode(null, right, right.getValue(), right.getHash());
    }

    if (left != null) {
      FinalizedNodeBranch node = new PendingNodeBranch(BigInteger.ONE, 0, left, right)
              .finalize(hashAlgorithm);
      return new SparseMerkleSumTreeRootNode(node.getLeft(), node.getRight(), node.getValue(),
              node.getHash());
    }

    return new SparseMerkleSumTreeRootNode(null, null, BigInteger.ZERO,
            new DataHash(hashAlgorithm, new byte[hashAlgorithm.getLength()]));
  }

  /**
   * Get root node left branch.
   *
   * @return left branch, or {@code null}
   */
  public FinalizedBranch getLeft() {
    return this.left;
  }

  /**
   * Get root node right branch.
   *
   * @return right branch, or {@code null}
   */
  public FinalizedBranch getRight() {
    return this.right;
  }

  /**
   * Get the total sum committed by the tree.
   *
   * @return root sum
   */
  public BigInteger getValue() {
    return this.value;
  }

  /**
   * Get root hash.
   *
   * @return root hash
   */
  public DataHash getHash() {
    return this.hash;
  }
}
