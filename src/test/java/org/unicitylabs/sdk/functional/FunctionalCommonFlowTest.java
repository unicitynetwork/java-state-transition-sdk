package org.unicitylabs.sdk.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.e2e.CommonTestFlow;

public class FunctionalCommonFlowTest {

  private StateTransitionClient client;

  @BeforeEach
  void setUp() {
    this.client = new StateTransitionClient(new TestAggregatorClient());
  }

  @Test
  void testTransferFlow() throws Exception {
    CommonTestFlow.testTransferFlow(this.client);
  }
}