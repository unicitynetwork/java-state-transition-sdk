package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.LeafExistsException;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * Sparse Merkle tree implementation.
 */
public class SparseMerkleTree {

  private Branch left = null;
  private Branch right = null;

  private final HashAlgorithm hashAlgorithm;

  /**
   * Create sparse Merkle tree with given hash algorithm.
   *
   * @param hashAlgorithm hash algorithm
   */
  public SparseMerkleTree(HashAlgorithm hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  /**
   * Add leaf to the tree at given path.
   *
   * @param key path of the leaf; must be a 32-byte key
   * @param data data of the leaf; arbitrary-length byte string
   * @throws LeafExistsException if a leaf already exists for the key
   * @throws IllegalArgumentException if the key is not 32 bytes
   */
  public synchronized void addLeaf(byte[] key, byte[] data) throws LeafExistsException {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(data, "data cannot be null");

    if (key.length != 32) {
      throw new IllegalArgumentException("Key must be 32 bytes long.");
    }

    key = Arrays.copyOf(key, key.length);
    data = Arrays.copyOf(data, data.length);

    boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, 0) == 1;
    Branch branch = isRight ? this.right : this.left;
    Branch result = branch != null
            ? SparseMerkleTree.buildTree(branch, key, data)
            : new PendingLeafBranch(key, data);

    if (isRight) {
      this.right = result;
    } else {
      this.left = result;
    }
  }

  /**
   * Calculate root of the tree.
   *
   * @return root node and its state
   */
  public synchronized SparseMerkleTreeRootNode calculateRoot() {
    FinalizedBranch left = this.left != null ? this.left.finalize(this.hashAlgorithm) : null;
    FinalizedBranch right = this.right != null ? this.right.finalize(this.hashAlgorithm) : null;
    this.left = left;
    this.right = right;

    FinalizedNodeBranch root = new PendingNodeBranch(new byte[32], 0, left, right)
            .finalize(hashAlgorithm);
    return SparseMerkleTreeRootNode.create(root);
  }

  private static Branch buildTree(Branch branch, byte[] key, byte[] value)
          throws LeafExistsException {
    if (branch instanceof LeafBranch) {
      int depth = branch.calculateSplitDepth(key);
      if (depth == branch.getDepth()) {
        throw new LeafExistsException();
      }

      boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, depth) == 1;
      LeafBranch newBranch = new PendingLeafBranch(key, value);
      return new PendingNodeBranch(SparseMerkleTreePathUtils.regionFromKey(key, depth), depth,
              isRight ? branch : newBranch, isRight ? newBranch : branch);
    }

    NodeBranch nodeBranch = (NodeBranch) branch;
    int depth = branch.calculateSplitDepth(key);
    boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, depth) == 1;

    // if node branch is split in the middle
    if (depth < branch.getDepth()) {
      LeafBranch newBranch = new PendingLeafBranch(key, value);
      return new PendingNodeBranch(SparseMerkleTreePathUtils.regionFromKey(key, depth), depth,
              isRight ? branch : newBranch, isRight ? newBranch : branch);
    }

    if (isRight) {
      return nodeBranch.withRightBranch(
              SparseMerkleTree.buildTree(nodeBranch.getRight(), key, value));
    }

    return nodeBranch.withLeftBranch(
            SparseMerkleTree.buildTree(nodeBranch.getLeft(), key, value));
  }
}
