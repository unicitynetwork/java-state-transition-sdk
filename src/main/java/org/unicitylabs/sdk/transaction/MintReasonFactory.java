package org.unicitylabs.sdk.transaction;

/**
 * Mint reason factory.
 */
public interface MintReasonFactory {

  /**
   * Create mint reason.
   *
   * @param bytes encoded mint reason
   * @return mint reason
   */
  MintTransactionReason create(byte[] bytes);
}
