package org.unicitylabs.sdk.mtree.plain;

import org.unicitylabs.sdk.hash.DataHash;

/**
 * Finalized branch in sparse merkle tree.
 */
interface FinalizedBranch extends Branch {

  /**
   * Get hash of the branch.
   *
   * @return hash
   */
  DataHash getHash();
}
