package org.unicitylabs.sdk.bft.verification.rule;

import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.verification.VerificationResult;
import org.unicitylabs.sdk.verification.VerificationRule;

public class InputRecordCurrentHashVerificationRule extends
    VerificationRule<UnicityCertificateVerificationContext> {

  public InputRecordCurrentHashVerificationRule() {
    this(null, null);
  }

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
