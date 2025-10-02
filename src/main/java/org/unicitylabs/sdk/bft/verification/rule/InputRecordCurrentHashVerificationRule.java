package org.unicitylabs.sdk.bft.verification.rule;

import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.verification.VerificationResult;
import org.unicitylabs.sdk.verification.VerificationRule;

/**
 * Input record current hash verification rule.
 */
public class InputRecordCurrentHashVerificationRule extends
    VerificationRule<UnicityCertificateVerificationContext> {

  /**
   * Create the rule without any subsequent rules.
   */
  public InputRecordCurrentHashVerificationRule() {
    this(null, null);
  }

  /**
   * Create the rule with subsequent rules for success and failure.
   *
   * @param onSuccessRule rule to execute on success
   * @param onFailureRule rule to execute on failure
   */
  public InputRecordCurrentHashVerificationRule(
      VerificationRule<UnicityCertificateVerificationContext> onSuccessRule,
      VerificationRule<UnicityCertificateVerificationContext> onFailureRule
  ) {
    super(
        "Verifying input record if current hash matches input hash.",
        onSuccessRule,
        onFailureRule
    );
  }

  @Override
  public VerificationResult verify(UnicityCertificateVerificationContext context) {
    if (context.getInputHash()
        .equals(DataHash.fromImprint(context.getUnicityCertificate().getInputRecord().getHash()))) {
      return VerificationResult.success();
    }

    return VerificationResult.fail("Input record current hash does not match input hash.");
  }

}
