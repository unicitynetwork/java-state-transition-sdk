package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.LeafExistsException;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

/**
 * Radix sparse Merkle sum tree. It reuses the radix sparse Merkle tree structure (big-endian key
 * routing, path-compressed binary trie, absolute bifurcation depths) and additionally commits a
 * positive amount at every leaf and the accumulated sum at every internal node, so any inclusion
 * proof also proves the leaf amount is part of the committed root total.
 */
public class SparseMerkleSumTree {

  private static final BigInteger VALUE_LIMIT = BigInteger.ONE.shiftLeft(256);

  private Branch left = null;
  private Branch right = null;

  private final HashAlgorithm hashAlgorithm;

  /**
   * Create radix sparse Merkle sum tree with given hash algorithm.
   *
   * @param hashAlgorithm hash algorithm
   */
  public SparseMerkleSumTree(HashAlgorithm hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   * Add a leaf to the tree.
   *
   * @param key 32-byte leaf key
   * @param data 32-byte leaf data
   * @param value leaf amount in the range {@code [1, 2^256)}
   * @throws LeafExistsException if a leaf already exists for the key
   * @throws IllegalArgumentException if the value is not a positive 256-bit integer, or the key
   *     or data is not 32 bytes
   */
  public synchronized void addLeaf(byte[] key, byte[] data, BigInteger value)
          throws LeafExistsException {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(data, "data cannot be null");
    Objects.requireNonNull(value, "value cannot be null");

    key = Arrays.copyOf(key, key.length);
    data = Arrays.copyOf(data, data.length);

    if (value.signum() <= 0 || value.compareTo(SparseMerkleSumTree.VALUE_LIMIT) >= 0) {
      throw new IllegalArgumentException("Value must be a positive 256-bit integer.");
    }

    if (key.length != 32) {
      throw new IllegalArgumentException("Key must be 32 bytes long.");
    }

    if (data.length != 32) {
      throw new IllegalArgumentException("Data must be 32 bytes long.");
    }

    boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, 0) == 1;
    Branch branch = isRight ? this.right : this.left;
    Branch result = branch != null
            ? SparseMerkleSumTree.buildTree(branch, key, data, value)
            : new PendingLeafBranch(key, data, value);

    if (isRight) {
      this.right = result;
    } else {
      this.left = result;
    }
  }

  /**
   * Calculate root of the tree.
   *
   * @return root node
   */
  public synchronized SparseMerkleSumTreeRootNode calculateRoot() {
    FinalizedBranch left = this.left != null ? this.left.finalize(this.hashAlgorithm) : null;
    FinalizedBranch right = this.right != null ? this.right.finalize(this.hashAlgorithm) : null;
    this.left = left;
    this.right = right;

    return SparseMerkleSumTreeRootNode.create(left, right, this.hashAlgorithm);
  }

  private static Branch buildTree(Branch branch, byte[] key, byte[] data, BigInteger value)
          throws LeafExistsException {
    if (branch instanceof LeafBranch) {
      int depth = branch.calculateSplitDepth(key);
      if (depth == branch.getDepth()) {
        throw new LeafExistsException();
      }

      boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, depth) == 1;
      LeafBranch newBranch = new PendingLeafBranch(key, data, value);
      return new PendingNodeBranch(SparseMerkleTreePathUtils.regionFromKey(key, depth), depth,
              isRight ? branch : newBranch, isRight ? newBranch : branch);
    }

    NodeBranch nodeBranch = (NodeBranch) branch;
    int depth = branch.calculateSplitDepth(key);
    boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, depth) == 1;

    // if node branch is split in the middle
    if (depth < branch.getDepth()) {
      LeafBranch newBranch = new PendingLeafBranch(key, data, value);
      return new PendingNodeBranch(SparseMerkleTreePathUtils.regionFromKey(key, depth), depth,
              isRight ? branch : newBranch, isRight ? newBranch : branch);
    }

    if (isRight) {
      return nodeBranch.withRightBranch(
              SparseMerkleSumTree.buildTree(nodeBranch.getRight(), key, data, value));
    }

    return nodeBranch.withLeftBranch(
            SparseMerkleSumTree.buildTree(nodeBranch.getLeft(), key, data, value));
  }
}
