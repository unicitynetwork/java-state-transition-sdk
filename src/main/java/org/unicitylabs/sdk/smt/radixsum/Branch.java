package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;

import java.math.BigInteger;

/**
 * Radix sparse Merkle sum tree branch structure.
 */
public interface Branch {

  /**
   * Get branch path from leaf to root.
   *
   * @return path
   */
  BigInteger getPath();

  /**
   * Finalize current branch.
   *
   * @param hashAlgorithm hash algorithm
   * @return finalized branch
   */
  FinalizedBranch finalize(HashAlgorithm hashAlgorithm);
}
