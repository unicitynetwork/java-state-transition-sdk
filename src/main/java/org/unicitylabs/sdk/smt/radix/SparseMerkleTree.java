package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.smt.BranchExistsException;
import org.unicitylabs.sdk.smt.CommonPath;
import org.unicitylabs.sdk.smt.LeafOutOfBoundsException;
import org.unicitylabs.sdk.util.BitString;

import java.math.BigInteger;
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
   * @param data data of the leaf; must be 32 bytes
   * @throws BranchExistsException    if branch already exists at the path
   * @throws LeafOutOfBoundsException if leaf is out of bounds
   * @throws IllegalArgumentException if the key or data is not 32 bytes, or the path is less than 1
   */
  public synchronized void addLeaf(byte[] key, byte[] data)
          throws BranchExistsException, LeafOutOfBoundsException {
    Objects.requireNonNull(key, "key cannot be null");
    Objects.requireNonNull(data, "data cannot be null");

    if (key.length != 32) {
      throw new IllegalArgumentException("Key must be 32 bytes long.");
    }

    if (data.length != 32) {
      throw new IllegalArgumentException("Data must be 32 bytes long.");
    }

    BigInteger path = BitString.fromBytesReversedLSB(key).toBigInteger();

    if (path.compareTo(BigInteger.ONE) <= 0) {
      throw new IllegalArgumentException("Path must be greater than 0");
    }

    boolean isRight = path.testBit(0);
    Branch branch = isRight ? this.right : this.left;
    Branch result = branch != null
            ? SparseMerkleTree.buildTree(branch, path, key, data)
            : new PendingLeafBranch(path, key, data);

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
  public synchronized FinalizedNodeBranch calculateRoot() {
    FinalizedBranch left = this.left != null ? this.left.finalize(this.hashAlgorithm) : null;
    FinalizedBranch right = this.right != null ? this.right.finalize(this.hashAlgorithm) : null;
    this.left = left;
    this.right = right;

    return new PendingNodeBranch(BigInteger.ONE, 0, left, right).finalize(hashAlgorithm);
  }

  private static Branch buildTree(Branch branch, BigInteger keyPath, byte[] key, byte[] value)
          throws BranchExistsException, LeafOutOfBoundsException {
    CommonPath commonPath = CommonPath.create(keyPath, branch.getPath());
    boolean isRight = keyPath.shiftRight(commonPath.getLength()).testBit(0);

    if (commonPath.getPath().equals(keyPath)) {
      throw new BranchExistsException();
    }

    if (branch instanceof LeafBranch) {
      if (commonPath.getPath().equals(branch.getPath())) {
        throw new LeafOutOfBoundsException();
      }

      LeafBranch newBranch = new PendingLeafBranch(keyPath, key, value);
      return new PendingNodeBranch(commonPath.getPath(), commonPath.getLength(),
              isRight ? branch : newBranch, isRight ? newBranch : branch);
    }

    NodeBranch nodeBranch = (NodeBranch) branch;

    // if node branch is split in the middle
    if (!commonPath.getPath().equals(branch.getPath())) {
      LeafBranch newBranch = new PendingLeafBranch(keyPath, key, value);
      return new PendingNodeBranch(commonPath.getPath(), commonPath.getLength(),
              isRight ? branch : newBranch, isRight ? newBranch : branch);
    }

    if (isRight) {
      return new PendingNodeBranch(nodeBranch.getPath(), nodeBranch.getDepth(),
              nodeBranch.getLeft(),
              SparseMerkleTree.buildTree(nodeBranch.getRight(), keyPath, key, value));
    }

    return new PendingNodeBranch(nodeBranch.getPath(), nodeBranch.getDepth(),
            SparseMerkleTree.buildTree(nodeBranch.getLeft(), keyPath, key, value),
            nodeBranch.getRight());
  }
}

