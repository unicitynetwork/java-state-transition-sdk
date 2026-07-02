package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.transaction.CertifiedMintTransaction;
import org.unicitylabs.sdk.transaction.TokenType;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.util.verification.VerificationResult;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Registry that dispatches token verification to the right {@link TokenIssuanceVerifier} based on
 * the token's type. A token type with no registered verifier is accepted, unless
 * {@code rejectUnregisteredTypes} is set, in which case it is rejected.
 */
public class TokenIssuanceVerifierService {

  private final Map<String, TokenIssuanceVerifier> verifiers = new HashMap<>();
  private final boolean rejectUnregisteredTypes;

  /**
   * Create a token issuance verifier registry that accepts unregistered token types.
   */
  public TokenIssuanceVerifierService() {
    this(false);
  }

  /**
   * Create a token issuance verifier registry.
   *
   * @param rejectUnregisteredTypes when {@code true}, reject any token whose type has no
   *     registered issuance verifier
   */
  public TokenIssuanceVerifierService(boolean rejectUnregisteredTypes) {
    this.rejectUnregisteredTypes = rejectUnregisteredTypes;
  }

  /**
   * Register a policy for its declared token type.
   *
   * @param verifier verifier to register
   *
   * @return this service for fluent chaining
   *
   * @throws IllegalArgumentException if a policy is already registered for the token type
   */
  public TokenIssuanceVerifierService register(TokenIssuanceVerifier verifier) {
    Objects.requireNonNull(verifier, "verifier cannot be null");
    String key = HexConverter.encode(verifier.getTokenType().getBytes());
    if (this.verifiers.containsKey(key)) {
      throw new IllegalArgumentException(String.format(
              "Duplicate token issuance verifier for token type %s.", verifier.getTokenType()));
    }

    this.verifiers.put(key, verifier);
    return this;
  }

  /**
   * Verify a token's genesis against the policy registered for its type.
   *
   * @param transaction genesis mint transaction whose token data to verify
   *
   * @return verification result
   */
  public VerificationResult<VerificationStatus> verify(CertifiedMintTransaction transaction) {
    TokenType tokenType = transaction.getTokenType();
    TokenIssuanceVerifier verifier = this.verifiers.get(
            HexConverter.encode(tokenType.getBytes()));
    if (verifier == null) {
      if (this.rejectUnregisteredTypes) {
        return new VerificationResult<>(
                "TokenIssuanceVerification",
                VerificationStatus.FAIL,
                String.format("No token issuance verifier registered for token type %s.",
                        tokenType));
      }

      return new VerificationResult<>("TokenIssuanceVerification", VerificationStatus.OK);
    }

    VerificationResult<VerificationStatus> result = verifier.verify(transaction);
    return new VerificationResult<>("TokenIssuanceVerification", result.getStatus(), "", result);
  }
}
