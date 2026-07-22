package org.unicitylabs.sdk.smt.radix;

/**
 * Node branch in merkle tree.
 */
interface NodeBranch extends Branch {

  /**
   * Get the node's committed region: its {@code depth}-bit key prefix with the suffix zeroed.
   *
   * @return region
   */
  byte[] getPath();

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

  /**
   * Derive a pending node with {@code left} as its left child, reusing this node's committed region.
   *
   * @param left replacement left child
   * @return new pending node
   */
  PendingNodeBranch withLeftBranch(Branch left);

  /**
   * Derive a pending node with {@code right} as its right child, reusing this node's committed
   * region.
   *
   * @param right replacement right child
   * @return new pending node
   */
  PendingNodeBranch withRightBranch(Branch right);
}
