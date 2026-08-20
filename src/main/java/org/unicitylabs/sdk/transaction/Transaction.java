package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.predicate.EncodedPredicate;

import java.util.Optional;

/**
 * Common interface for token transactions.
 */
public interface Transaction {

  /**
   * Get transaction payload bytes.
   *
   * @return payload bytes
   */
  Optional<byte[]> getData();

  /**
   * Gets the predicate that locks this transaction.
   *
   * @return lock script predicate
   */
  EncodedPredicate getLockScript();

  /**
   * Gets the transaction recipient.
   *
   * @return recipient predicate
   */
  EncodedPredicate getRecipient();

  /**
   * Gets the source state hash.
   *
   * @return source state hash
   */
  DataHash getSourceStateHash();

  /**
   * Get transaction randomness component.
   *
   * @return randomness bytes
   */
  StateMask getStateMask();

  /**
   * Exclusive timeout of the certification request. The Unicity Service admits the request only
   * in a round whose reference time is below this value. It is part of the transaction encoding,
   * so the transaction hash commits to it and the unlock script signs it.
   *
   * @return request timeout
   */
  long getTimeout();

  /**
   * Calculates the resulting state hash.
   *
   * @return state hash
   */
  DataHash calculateStateHash();

  /**
   * Calculates the transaction hash.
   *
   * @return transaction hash
   */
  DataHash calculateTransactionHash();

  /**
   * Serializes this transaction as CBOR.
   *
   * @return CBOR bytes
   */
  byte[] toCbor();
}
