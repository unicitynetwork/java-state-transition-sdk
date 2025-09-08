package org.unicitylabs.sdk.functional;

import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.common.split.BaseTokenSplitTest;
import org.junit.jupiter.api.BeforeEach;

public class FunctionalTokenSplitTest extends BaseTokenSplitTest {
  @BeforeEach
  void setUp() {
    this.client = new StateTransitionClient(new TestAggregatorClient());
  }
}
