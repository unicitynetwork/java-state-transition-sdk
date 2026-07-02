package org.unicitylabs.sdk.smt.radixsum;

/**
 * Node branch in a radix sparse Merkle sum tree.
 */
public interface NodeBranch extends Branch {

  /**
   * Get the absolute bifurcation depth of this node.
   *
   * @return depth
   */
  int getDepth();

  /**
   * Get left branch.
   *
   * @return left branch
   */
  Branch getLeft();

  /**
   * Get right branch.
   *
   * @return right branch
   */
  Branch getRight();
}
