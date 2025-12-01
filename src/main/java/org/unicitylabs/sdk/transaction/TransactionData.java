package org.unicitylabs.sdk.transaction;

import java.util.Optional;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * Interface representing the data of a transaction.
 *
 * @param <T> the type of the transaction source state
 */
public interface TransactionData<T> {

  /**
   * Gets the transaction source state.
   *
   * @return the source state
   */
  T getSourceState();

  /**
   * Gets the recipient address of the transaction.
   *
   * @return the recipient address
   */
  Address getRecipient();

  /**
   * Get transaction salt.
   *
   * @return transaction salt
   */
  byte[] getSalt();

  /**
   * Gets the optional recipient data hash.
   *
   * @return an Optional containing the data hash if present, otherwise empty
   */
  Optional<DataHash> getRecipientDataHash();

  /**
   * Calculates the hash of the transaction data.
   *
   * @return the calculated DataHash
   */
  DataHash calculateHash();

  /**
   * Convert transaction data to CBOR bytes.
   *
   * @return CBOR bytes
   */
  byte[] toCbor();
}
