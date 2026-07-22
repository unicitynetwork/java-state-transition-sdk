package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.transaction.CertifiedMintTransaction;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.util.verification.VerificationResult;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

import java.util.function.Consumer;

/**
 * Verifier for a specific kind of certified mint transaction justification, identified by a CBOR
 * tag. Implementations are registered with {@link MintJustificationVerifierService} and dispatched
 * based on the tag of the bytes stored in the mint transaction's justification field.
 */
public interface MintJustificationVerifier {

  /**
   * Get the CBOR tag identifying the justification kind handled by this verifier.
   *
   * @return CBOR tag
   */
  long getTag();

  /**
   * Verify the justification of the given certified mint transaction.
   *
   * <p>Implementations must not verify tokens embedded in the justification (for example, the
   * burn token of a split) recursively. Instead they must pass each embedded token to
   * {@code nestedTokenCollector}; the caller verifies them iteratively.
   *
   * @param transaction certified mint transaction whose justification is being verified
   * @param nestedTokenCollector collector receiving tokens embedded in the justification that the
   *     caller must verify
   *
   * @return verification result
   */
  VerificationResult<VerificationStatus> verify(
          CertifiedMintTransaction transaction,
          Consumer<Token> nestedTokenCollector);
}
