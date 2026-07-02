package org.unicitylabs.sdk.smt.radixsum;

import org.unicitylabs.sdk.crypto.hash.DataHash;

import java.math.BigInteger;

/**
 * Finalized branch in a radix sparse Merkle sum tree.
 */
public interface FinalizedBranch extends Branch {

  /**
   * Get hash of the branch.
   *
   * @return hash
   */
  DataHash getHash();

  /**
   * Get the sum committed by this branch.
   *
   * @return sum
   */
  BigInteger getValue();
}
