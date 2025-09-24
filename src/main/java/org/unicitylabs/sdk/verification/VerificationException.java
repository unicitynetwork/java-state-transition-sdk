package org.unicitylabs.sdk.verification;

import java.util.Objects;

public class VerificationException extends Exception {

  private final VerificationResult verificationResult;

  public VerificationException(String message, VerificationResult verificationResult) {
    super(message);
    Objects.requireNonNull(verificationResult, "verificationResult cannot be null");

    this.verificationResult = verificationResult;
  }

  public VerificationResult getVerificationResult() {
    return verificationResult;
  }
}
