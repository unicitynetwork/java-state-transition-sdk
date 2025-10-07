package org.unicitylabs.sdk.verification;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite verification rule that chains multiple verification rules together.
 *
 * <p>This class allows you to create a sequence of verification rules where each rule can lead to
 * another rule based on the result of the verification. The first rule in the chain is provided at
 * construction, and subsequent rules can be determined dynamically based on the outcome of each
 * verification step.
 *
 * <p>When the {@code verify} method is called, it starts with the first rule and continues to
 * execute subsequent rules based on whether the previous rule was successful or not. The final
 * result is a composite {@code VerificationResult} that includes the results of all executed
 * rules.
 *
 * @param <C> the type of context used for verification
 */
public abstract class CompositeVerificationRule<C extends VerificationContext>
    extends VerificationRule<C> {

  private final VerificationRule<C> firstRule;
  private final String message;

  /**
   * Constructs a {@code CompositeVerificationRule} with the specified message and the first rule in
   * the chain.
   *
   * @param message   a descriptive message for the composite rule
   * @param firstRule the first verification rule to execute in the chain
   */
  public CompositeVerificationRule(
      String message,
      VerificationRule<C> firstRule
  ) {
    super(message);

    this.firstRule = firstRule;
    this.message = message;
  }

  @Override
  public VerificationResult verify(C context) {
    VerificationRule<C> rule = this.firstRule;
    List<VerificationResult> results = new ArrayList<>();

    while (rule != null) {
      VerificationResult result = rule.verify(context);
      results.add(result);
      rule = rule.getNextRule(result.isSuccessful()
          ? VerificationResultCode.OK
          : VerificationResultCode.FAIL);
    }

    return VerificationResult.fromChildren(this.message, results);
  }
}