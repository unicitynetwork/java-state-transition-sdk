package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * Predicate reference interface.
 */
public interface PredicateReference {

  /**
   * Get predicate reference as hash.
   *
   * @return reference hash
   */
  DataHash getHash();

  /**
   * Get predicate reference as address.
   *
   * @return reference address
   */
  Address toAddress();
}
