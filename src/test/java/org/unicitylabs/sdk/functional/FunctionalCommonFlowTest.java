package org.unicitylabs.sdk.functional;

import org.junit.jupiter.api.BeforeEach;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.common.CommonTestFlow;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.transaction.verification.MintJustificationVerifierService;
import org.unicitylabs.sdk.transaction.verification.TokenIssuanceVerifierService;
import org.unicitylabs.sdk.transaction.verification.VerificationContext;

public class FunctionalCommonFlowTest extends CommonTestFlow {

  @BeforeEach
  void setUp() {
    TestAggregatorClient aggregatorClient = TestAggregatorClient.create();
    this.client = new StateTransitionClient(aggregatorClient);
    this.context = new VerificationContext(aggregatorClient.getTrustBase(),
            PredicateVerifierService.create(), new MintJustificationVerifierService(),
            new TokenIssuanceVerifierService());
  }
}