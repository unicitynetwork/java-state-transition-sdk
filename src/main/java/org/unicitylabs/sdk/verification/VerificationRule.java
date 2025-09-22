package org.unicitylabs.sdk.verification;

public abstract class VerificationRule<CTX> {

  private final VerificationRule<CTX> onSuccessRule;
  private final VerificationRule<CTX> onFailureRule;

  protected VerificationRule(VerificationRule<CTX> rule) {
    this(
        rule.onSuccessRule,
        rule.onFailureRule
    );
  }

  protected VerificationRule(
      VerificationRule<CTX> onSuccessRule,
      VerificationRule<CTX> onFailureRule
  ) {
    this.onSuccessRule = onSuccessRule;
    this.onFailureRule = onFailureRule;
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