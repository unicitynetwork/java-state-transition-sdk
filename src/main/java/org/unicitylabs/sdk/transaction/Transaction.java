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
   * Exclusive certification request deadline in Unix seconds, or empty when the Unicity Service
   * assigns one from consensus time. It occupies a fixed position in the encoding and is committed
   * by the transaction hash either way.
   *
   * @return request deadline
   */
  Optional<Long> getExpiresAt();

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
