package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.smt.SparseMerkleTreePathUtils;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Root of a radix sparse Merkle tree: the committed root hash plus the ability to enumerate the
 * root-to-leaf sibling path for a key. The tree's branch nodes are an internal detail of this
 * package; callers see only the root hash and the sibling path.
 */
public class SparseMerkleTreeRootNode {

  private final FinalizedNodeBranch root;

  private SparseMerkleTreeRootNode(FinalizedNodeBranch root) {
    this.root = root;
  }

  static SparseMerkleTreeRootNode create(FinalizedNodeBranch root) {
    return new SparseMerkleTreeRootNode(root);
  }

  /**
   * Get the root hash.
   *
   * @return root hash
   */
  public DataHash getHash() {
    return this.root.getHash();
  }

  /**
   * Enumerate the sibling entries on the path from the root to the leaf with the given key, ordered
   * from the root down to the leaf.
   *
   * @param key 32-byte leaf key
   * @return sibling entries from the root down to the leaf
   * @throws IllegalArgumentException if the key is not present in the tree
   */
  public List<Sibling> getPath(byte[] key) {
    Objects.requireNonNull(key, "key cannot be null");

    List<Sibling> siblings = new ArrayList<>();
    FinalizedBranch node = this.root;
    while (node != null) {
      if (node instanceof FinalizedLeafBranch) {
        if (!Arrays.equals(((FinalizedLeafBranch) node).getKey(), key)) {
          throw new IllegalArgumentException(
                  String.format("Leaf not found for key: %s", HexConverter.encode(key)));
        }
        return siblings;
      }

      FinalizedNodeBranch branch = (FinalizedNodeBranch) node;
      int depth = branch.getDepth();
      boolean isRight = SparseMerkleTreePathUtils.getBitAtDepth(key, depth) == 1;
      FinalizedBranch sibling = isRight ? branch.getLeft() : branch.getRight();
      if (sibling != null) {
        siblings.add(new Sibling(depth, sibling.getHash()));
      }
      node = isRight ? branch.getRight() : branch.getLeft();
    }

    throw new IllegalArgumentException(
            String.format("Leaf not found for key: %s", HexConverter.encode(key)));
  }

  /**
   * One sibling entry of a radix sparse Merkle tree inclusion path.
   */
  public static final class Sibling {
    private final int depth;
    private final DataHash hash;

    private Sibling(int depth, DataHash hash) {
      this.depth = depth;
      this.hash = hash;
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
  }
}
