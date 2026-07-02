package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.BranchExistsException;
import org.unicitylabs.sdk.smt.CommonPath;
import org.unicitylabs.sdk.smt.LeafOutOfBoundsException;
import org.unicitylabs.sdk.util.BitString;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Radix sparse Merkle sum tree. It reuses the radix sparse Merkle tree structure (LSB-first key
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
   * @throws BranchExistsException if a branch already exists at the key's path
   * @throws LeafOutOfBoundsException if the leaf is out of bounds
   * @throws IllegalArgumentException if the value is not a positive 256-bit integer, or the key
   *     or data is not 32 bytes
   */
  public synchronized void addLeaf(byte[] key, byte[] data, BigInteger value)
          throws BranchExistsException, LeafOutOfBoundsException {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(data, "data cannot be null");
    Objects.requireNonNull(value, "value cannot be null");

    if (value.signum() <= 0 || value.compareTo(SparseMerkleSumTree.VALUE_LIMIT) >= 0) {
      throw new IllegalArgumentException("Value must be a positive 256-bit integer.");
    }

    if (key.length != 32) {
      throw new IllegalArgumentException("Key must be 32 bytes long.");
    }

    if (data.length != 32) {
      throw new IllegalArgumentException("Data must be 32 bytes long.");
    }

    BigInteger path = BitString.fromBytesReversedLSB(key).toBigInteger();

    boolean isRight = path.testBit(0);
    Branch branch = isRight ? this.right : this.left;
    Branch result = branch != null
            ? SparseMerkleSumTree.buildTree(branch, path, 0, key, data, value)
            : new PendingLeafBranch(path, key, data, value);

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

  private static Branch buildTree(Branch branch, BigInteger remainingPath, int depth, byte[] key,
                                  byte[] data, BigInteger value)
          throws BranchExistsException, LeafOutOfBoundsException {
    CommonPath commonPath = CommonPath.create(remainingPath, branch.getPath());
    int commonPathLength = commonPath.getLength();
    boolean isRight = remainingPath.shiftRight(commonPathLength).testBit(0);

    if (commonPath.getPath().equals(remainingPath)) {
      throw new BranchExistsException();
    }

    if (branch instanceof LeafBranch) {
      if (commonPath.getPath().equals(branch.getPath())) {
        throw new LeafOutOfBoundsException();
      }

      LeafBranch leafBranch = (LeafBranch) branch;

      LeafBranch oldBranch = new PendingLeafBranch(
              branch.getPath().shiftRight(commonPathLength), leafBranch.getKey(),
              leafBranch.getData(), leafBranch.getValue());
      LeafBranch newBranch = new PendingLeafBranch(
              remainingPath.shiftRight(commonPathLength), key, data, value);
      return new PendingNodeBranch(commonPath.getPath(), depth + commonPathLength,
              isRight ? oldBranch : newBranch, isRight ? newBranch : oldBranch);
    }

    NodeBranch nodeBranch = (NodeBranch) branch;

    // if node branch is split in the middle
    if (commonPath.getPath().compareTo(branch.getPath()) < 0) {
      LeafBranch newBranch = new PendingLeafBranch(
              remainingPath.shiftRight(commonPathLength), key, data, value);
      NodeBranch oldBranch = new PendingNodeBranch(
              branch.getPath().shiftRight(commonPathLength), nodeBranch.getDepth(),
              nodeBranch.getLeft(), nodeBranch.getRight());
      return new PendingNodeBranch(commonPath.getPath(), depth + commonPathLength,
              isRight ? oldBranch : newBranch, isRight ? newBranch : oldBranch);
    }

    if (isRight) {
      return new PendingNodeBranch(nodeBranch.getPath(), nodeBranch.getDepth(),
              nodeBranch.getLeft(),
              SparseMerkleSumTree.buildTree(nodeBranch.getRight(),
                      remainingPath.shiftRight(commonPathLength), depth + commonPathLength, key,
                      data, value));
    }

    return new PendingNodeBranch(nodeBranch.getPath(), nodeBranch.getDepth(),
            SparseMerkleSumTree.buildTree(nodeBranch.getLeft(),
                    remainingPath.shiftRight(commonPathLength), depth + commonPathLength, key,
                    data, value),
            nodeBranch.getRight());
  }
}
