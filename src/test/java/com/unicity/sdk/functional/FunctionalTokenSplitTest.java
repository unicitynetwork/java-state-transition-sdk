package com.unicity.sdk.functional;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.TestAggregatorClient;
import com.unicity.sdk.common.split.TokenSplitTest;
import org.junit.jupiter.api.BeforeEach;

public class FunctionalTokenSplitTest extends TokenSplitTest {
  @BeforeEach
  void setUp() {
    this.client = new StateTransitionClient(new TestAggregatorClient());
  }
}
