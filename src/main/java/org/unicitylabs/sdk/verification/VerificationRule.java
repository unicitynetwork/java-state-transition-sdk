package org.unicitylabs.sdk.verification;

import java.util.Objects;

/**
 * Verification rule base class.
 *
 * @param <ContextT> verification context
 */
public abstract class VerificationRule<ContextT> {

  private final String message;
  private final VerificationRule<ContextT> onSuccessRule;
  private final VerificationRule<ContextT> onFailureRule;

  /**
   * Create the rule without any subsequent rules.
   *
   * @param message rule message
   */
  protected VerificationRule(String message) {
    this(message, null, null);
  }

  /**
   * Create the rule with subsequent rules for success and failure.
   *
   * @param message       rule message
   * @param onSuccessRule rule to execute on success
   * @param onFailureRule rule to execute on failure
   */
  protected VerificationRule(
      String message,
      VerificationRule<ContextT> onSuccessRule,
      VerificationRule<ContextT> onFailureRule
  ) {
    Objects.requireNonNull(message, "Message cannot be null");

    this.message = message;
    this.onSuccessRule = onSuccessRule;
    this.onFailureRule = onFailureRule;
  }

  /**
   * Get verification rule message.
   *
   * @return message
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Get next verification rule based on verification result.
   *
   * @param resultCode result of current verification rule
   * @return next rule or null if no rule exists for given result
   */
  public VerificationRule<ContextT> getNextRule(VerificationResultCode resultCode) {
    switch (resultCode) {
      case OK:
        return this.onSuccessRule;
      case FAIL:
        return this.onFailureRule;
      default:
        return null;
    }
  }

  /**
   * Verify context against current rule.
   *
   * @param context verification context
   * @return verification result
   */
  public abstract VerificationResult verify(ContextT context);
}