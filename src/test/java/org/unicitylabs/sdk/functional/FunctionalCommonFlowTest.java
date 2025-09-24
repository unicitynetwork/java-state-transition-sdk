package org.unicitylabs.sdk.functional;

import org.junit.jupiter.api.BeforeEach;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.e2e.CommonTestFlow;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.utils.RootTrustBaseUtils;

public class FunctionalCommonFlowTest extends CommonTestFlow {

  @BeforeEach
  void setUp() {
    SigningService signingService = new SigningService(SigningService.generatePrivateKey());
    this.client = new StateTransitionClient(new TestAggregatorClient(signingService));
    this.trustBase = RootTrustBaseUtils.generateRootTrustBase(signingService.getPublicKey());
  }
}