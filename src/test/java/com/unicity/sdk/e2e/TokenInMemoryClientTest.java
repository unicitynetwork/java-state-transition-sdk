package com.unicity.sdk.e2e;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.TestAggregatorClient;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests for token operations using CommonTestFlow. Matches TypeScript SDK's test
 * structure.
 */
public class TokenInMemoryClientTest {

  private StateTransitionClient client = new StateTransitionClient(new TestAggregatorClient());

  @Test
  void testTransferFlow() throws Exception {
    CommonTestFlow.testTransferFlow(this.client);
  }

}