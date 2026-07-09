package org.unicitylabs.sdk.smt.radixsum;

import java.math.BigInteger;

/**
 * Leaf branch in a radix sparse Merkle sum tree.
 */
interface LeafBranch extends Branch {

  /**
   * Get the 32-byte leaf key.
   *
   * @return key
   */
  byte[] getKey();

  /**
   * Get the 32-byte leaf data.
   *
   * @return data
   */
  byte[] getData();

  /**
   * Get the leaf amount.
   *
   * @return amount
   */
  BigInteger getValue();
}
