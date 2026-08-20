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
   * Explicit exclusive timeout of the certification request, or zero for the service default.
   * Explicit values are committed by the v2 transaction encoding.
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
