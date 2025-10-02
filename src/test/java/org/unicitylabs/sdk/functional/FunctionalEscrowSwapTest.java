package org.unicitylabs.sdk.functional;

import org.junit.jupiter.api.BeforeEach;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.common.BaseEscrowSwapTest;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.bft.RootTrustBaseUtils;

public class FunctionalEscrowSwapTest extends BaseEscrowSwapTest {
  @BeforeEach
  void setUp() {
    SigningService signingService = new SigningService(SigningService.generatePrivateKey());
    this.client = new StateTransitionClient(new TestAggregatorClient(signingService));
    this.trustBase = RootTrustBaseUtils.generateRootTrustBase(signingService.getPublicKey());
  }
}
