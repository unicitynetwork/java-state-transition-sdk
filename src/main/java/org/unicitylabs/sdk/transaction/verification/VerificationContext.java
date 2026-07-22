package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.api.bft.RootTrustBase;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;

import java.util.Objects;

/**
 * Immutable bundle of the dependencies shared across a (possibly nested) token verification: the
 * single root of trust, the predicate verifier, and the mint-justification and token-issuance
 * registries. It holds no mutable state, so a nested (e.g. burned source) token is always
 * verified under the same root of trust and registries as the outer token.
 *
 * <p>All verifiers must be supplied explicitly: a defaulted token-issuance verifier would be an
 * empty fail-closed registry that rejects every token, so there is no sensible default.
 */
public final class VerificationContext {

  private final RootTrustBase trustBase;
  private final PredicateVerifierService predicateVerifier;
  private final MintJustificationVerifierService mintJustificationVerifier;
  private final TokenIssuanceVerifierService tokenIssuanceVerifier;

  /**
   * Create a verification context with all dependencies.
   *
   * @param trustBase root trust base for the network
   * @param predicateVerifier predicate verifier
   * @param mintJustificationVerifier mint justification registry
   * @param tokenIssuanceVerifier token issuance registry
   */
  public VerificationContext(
          RootTrustBase trustBase,
          PredicateVerifierService predicateVerifier,
          MintJustificationVerifierService mintJustificationVerifier,
          TokenIssuanceVerifierService tokenIssuanceVerifier
  ) {
    this.trustBase = Objects.requireNonNull(trustBase, "trustBase cannot be null");
    this.predicateVerifier = Objects.requireNonNull(predicateVerifier,
            "predicateVerifier cannot be null");
    this.mintJustificationVerifier = Objects.requireNonNull(mintJustificationVerifier,
            "mintJustificationVerifier cannot be null");
    this.tokenIssuanceVerifier = Objects.requireNonNull(tokenIssuanceVerifier,
            "tokenIssuanceVerifier cannot be null");
  }

  /**
   * Get the root trust base.
   *
   * @return trust base
   */
  public RootTrustBase getTrustBase() {
    return this.trustBase;
  }

  /**
   * Get the predicate verifier.
   *
   * @return predicate verifier
   */
  public PredicateVerifierService getPredicateVerifier() {
    return this.predicateVerifier;
  }

  /**
   * Get the mint justification registry.
   *
   * @return mint justification verifier
   */
  public MintJustificationVerifierService getMintJustificationVerifier() {
    return this.mintJustificationVerifier;
  }

  /**
   * Get the token issuance registry.
   *
   * @return token issuance verifier
   */
  public TokenIssuanceVerifierService getTokenIssuanceVerifier() {
    return this.tokenIssuanceVerifier;
  }
}
