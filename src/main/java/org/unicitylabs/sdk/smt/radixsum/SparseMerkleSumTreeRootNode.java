package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;
import org.unicitylabs.sdk.util.HexConverter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Radix sparse Merkle sum tree root node. If the tree holds a single leaf the root hash and sum
 * are that leaf's; otherwise the root is the top internal node, which bifurcates at depth 0. The
 * tree's branch nodes are an internal detail of this package; callers see only the root hash, the
 * root sum and the sibling path.
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
      FinalizedNodeBranch node = new PendingNodeBranch(new byte[32], 0, left, right)
              .finalize(hashAlgorithm);
      return new SparseMerkleSumTreeRootNode(node.getLeft(), node.getRight(), node.getValue(),
              node.getHash());
    }

    return new SparseMerkleSumTreeRootNode(null, null, BigInteger.ZERO,
            new DataHash(hashAlgorithm, new byte[hashAlgorithm.getLength()]));
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

  /**
   * Enumerate the sibling entries on the path from the leaf with the given key up to the root,
   * ordered from the leaf up to the root (strictly decreasing depth).
   *
   * @param key 32-byte leaf key
   * @return sibling entries from the leaf up to the root
   * @throws IllegalArgumentException if the key is not present in the tree
   */
  public List<Sibling> getPath(byte[] key) {
    Objects.requireNonNull(key, "key cannot be null");

    List<Sibling> siblings = new ArrayList<>();
    boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, 0) == 1;
    FinalizedBranch sibling = isRight ? this.left : this.right;
    FinalizedBranch node = isRight ? this.right : this.left;
    if (sibling != null) {
      siblings.add(new Sibling(0, sibling.getHash(), sibling.getValue()));
    }

    while (node instanceof FinalizedNodeBranch) {
      FinalizedNodeBranch branch = (FinalizedNodeBranch) node;
      int depth = branch.getDepth();
      isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, depth) == 1;
      sibling = isRight ? branch.getLeft() : branch.getRight();
      node = isRight ? branch.getRight() : branch.getLeft();
      if (sibling != null) {
        siblings.add(new Sibling(depth, sibling.getHash(), sibling.getValue()));
      }
    }

    if (!(node instanceof FinalizedLeafBranch)) {
      throw new IllegalArgumentException(
              "Could not construct split allocation proof: invalid path.");
    }
    if (!Arrays.equals(((FinalizedLeafBranch) node).getKey(), key)) {
      throw new IllegalArgumentException(
              String.format("Leaf not found for key: %s", HexConverter.encode(key)));
    }

    Collections.reverse(siblings);
    return siblings;
  }

  /**
   * One sibling entry of a radix sparse Merkle sum tree inclusion path.
   */
  public static final class Sibling {
    private final int depth;
    private final DataHash hash;
    private final BigInteger value;

    private Sibling(int depth, DataHash hash, BigInteger value) {
      this.depth = depth;
      this.hash = hash;
      this.value = value;
    }

    /**
     * Get the bifurcation depth at which this sibling hangs.
     *
     * @return depth
     */
    public int getDepth() {
      return this.depth;
    }

    /**
     * Get the sibling subtree hash.
     *
     * @return hash
     */
    public DataHash getHash() {
      return this.hash;
    }

    /**
     * Get the sum committed by the sibling subtree.
     *
     * @return sum
     */
    public BigInteger getValue() {
      return this.value;
    }
  }
}
