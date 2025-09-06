package com.unicity.sdk.functional;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.TestAggregatorClient;
import com.unicity.sdk.common.split.BaseTokenSplitTest;
import org.junit.jupiter.api.BeforeEach;

public class FunctionalTokenSplitTest extends BaseTokenSplitTest {
  @BeforeEach
  void setUp() {
    this.client = new StateTransitionClient(new TestAggregatorClient());
  }
}
