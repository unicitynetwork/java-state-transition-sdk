package org.unicitylabs.sdk.verification;

import java.util.Objects;

/**
 * Exception thrown when a verification fails.
 */
public class VerificationException extends Exception {

  /**
   * Verification result.
   */
  private final VerificationResult verificationResult;

  /**
   * Create exception with message and verification result.
   *
   * @param message            message
   * @param verificationResult verification result
   */
  public VerificationException(String message, VerificationResult verificationResult) {
    super(message);
    Objects.requireNonNull(verificationResult, "verificationResult cannot be null");

    this.verificationResult = verificationResult;
  }

  /**
   * Get verification result.
   *
   * @return verification result
   */
  public VerificationResult getVerificationResult() {
    return verificationResult;
  }
}
