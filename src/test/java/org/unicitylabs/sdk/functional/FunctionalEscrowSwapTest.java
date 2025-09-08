package org.unicitylabs.sdk.functional;

import org.junit.jupiter.api.BeforeEach;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.common.BaseEscrowSwapTest;

public class FunctionalEscrowSwapTest extends BaseEscrowSwapTest {
  @BeforeEach
  void setUp() {
    this.client = new StateTransitionClient(new TestAggregatorClient());
  }
}
