package org.unicitylabs.sdk.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.common.BaseEscrowSwap;

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class E2EEscrowSwapTest extends BaseEscrowSwap {
  @BeforeEach
  void setUp() {
    String aggregatorUrl = System.getenv("AGGREGATOR_URL");
    Assertions.assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

    this.client = new StateTransitionClient(new AggregatorClient(aggregatorUrl));
  }
}
