package org.unicitylabs.sdk.verification;

import java.util.Objects;

public abstract class VerificationRule<CTX> {

  private final String message;
  private final VerificationRule<CTX> onSuccessRule;
  private final VerificationRule<CTX> onFailureRule;

  protected VerificationRule(String message) {
    this(message, null, null);
  }

  protected VerificationRule(
      String message,
      VerificationRule<CTX> onSuccessRule,
      VerificationRule<CTX> onFailureRule
  ) {
    Objects.requireNonNull(message, "Message cannot be null");

    this.message = message;
    this.onSuccessRule = onSuccessRule;
    this.onFailureRule = onFailureRule;
  }

  public String getMessage() {
    return this.message;
  }

  public VerificationRule<CTX> getNextRule(VerificationResultCode resultCode) {
    switch (resultCode) {
      case OK:
        return this.onSuccessRule;
      case FAIL:
        return this.onFailureRule;
      default:
        return null;
    }
  }

  public abstract VerificationResult verify(CTX context);
}