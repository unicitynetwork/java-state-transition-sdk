package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.transaction.CertifiedMintTransaction;
import org.unicitylabs.sdk.transaction.TokenType;
import org.unicitylabs.sdk.util.verification.VerificationResult;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

/**
 * Application-supplied policy for tokens of a single {@link TokenType}. Plugged into
 * {@link TokenIssuanceVerifierService}, which dispatches by token type and decides whether a
 * token's genesis data is acceptable for its type.
 *
 * <p>Registering a policy is an application trust decision: cryptographic certification alone
 * does not authorize an issuance, so a payment consumer should verify both the payload structure
 * and its own issuance policy here.
 */
public interface TokenIssuanceVerifier {

  /**
   * Get the token type this policy applies to.
   *
   * @return token type
   */
  TokenType getTokenType();

  /**
   * Verify the genesis data and any application-level issuance policy.
   *
   * @param transaction genesis mint transaction to verify
   *
   * @return verification result
   */
  VerificationResult<VerificationStatus> verify(CertifiedMintTransaction transaction);
}
