package org.unicitylabs.sdk.smt.radix;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;

/**
 * Sparse merkle tree branch structure.
 */
interface Branch {

  /**
   * Get the absolute bifurcation depth of this branch.
   *
   * @return depth
   */
  int getDepth();

  /**
   * Depth at which {@code key} diverges from this branch, capped at the branch's own depth.
   *
   * @param key key being inserted
   * @return common-prefix depth
   */
  int calculateSplitDepth(byte[] key);

  /**
   * Finalize current branch.
   *
   * @param hashAlgorithm hash algorithm
   * @return finalized branch
   */
  FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
