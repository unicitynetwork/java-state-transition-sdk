package org.unicitylabs.sdk.verification;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeVerificationRule<CTX> extends VerificationRule<CTX> {

  private final VerificationRule<CTX> firstRule;
  private final String message;

  public CompositeVerificationRule(
      String message,
      VerificationRule<CTX> firstRule
  ) {
    super(message);

    this.firstRule = firstRule;
    this.message = message;
  }

  public VerificationResult verify(CTX context) {
    VerificationRule<CTX> rule = this.firstRule;
    List<VerificationResult> results = new ArrayList<>();

    while (rule != null) {
      VerificationResult result = rule.verify(context);
      results.add(result);
      rule = rule.getNextRule(result.isSuccessful() ? VerificationResultCode.OK : VerificationResultCode.FAIL);
    }

    return VerificationResult.fromChildren(this.message, results);
  }
}