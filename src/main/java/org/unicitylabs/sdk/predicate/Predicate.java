package org.unicitylabs.sdk.predicate;

import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.transaction.TransferTransaction;

/**
 * Predicate structure.
 */
public interface Predicate extends SerializablePredicate {

  /**
   * Calculate predicate hash representation.
   *
   * @return predicate hash
   */
  DataHash calculateHash();

  /**
   * Get predicate as reference.
   *
   * @return predicate reference
   */
  PredicateReference getReference();

  /**
   * Is given public key owner of current predicate.
   *
   * @param publicKey public key of potential owner
   * @return true if is owner
   */
  boolean isOwner(byte[] publicKey);

  /**
   * Verify if predicate is valid for given token state.
   *
   * @param token       current token state
   * @param transaction current transaction
   * @param trustBase   trust base to verify against.
   * @return true if successful
   */
  boolean verify(Token token, TransferTransaction transaction, RootTrustBase trustBase);
}

