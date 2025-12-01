package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.verification.VerificationResult;

/**
 * Mint transaction reason.
 */
public interface MintTransactionReason {
  /**
   * Verify mint reason for genesis.
   *
   * @param genesis Genesis to verify against
   * @return verification result
   */
  VerificationResult verify(MintTransaction genesis);

  /**
   * Convert mint transaction reason to CBOR bytes.
   *
   * @return CBOR representation of reason
   */
  byte[] toCbor();
}
