package org.unicitylabs.sdk.bft.verification;

import org.unicitylabs.sdk.bft.verification.rule.InputRecordCurrentHashVerificationRule;
import org.unicitylabs.sdk.bft.verification.rule.UnicitySealHashMatchesWithRootHashRule;
import org.unicitylabs.sdk.bft.verification.rule.UnicitySealQuorumSignaturesVerificationRule;
import org.unicitylabs.sdk.verification.CompositeVerificationRule;

public class UnicityCertificateVerificationRule extends
    CompositeVerificationRule<UnicityCertificateVerificationContext> {

  public UnicityCertificateVerificationRule() {
    super("Verify unicity certificate",
        new InputRecordCurrentHashVerificationRule(
            new UnicitySealHashMatchesWithRootHashRule(
                new UnicitySealQuorumSignaturesVerificationRule(),
                null
            ),
            null
        ));
  }
}
