package org.unicitylabs.sdk.e2e;

import java.io.IOException;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.common.CommonTestFlow;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for token operations using CommonTestFlow. Matches TypeScript SDK's test
 * structure.
 */

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class TokenE2ETest extends CommonTestFlow {

  private AggregatorClient aggregatorClient;

  @BeforeEach
  void setUp() throws IOException {
    String aggregatorUrl = System.getenv("AGGREGATOR_URL");
    assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

    this.aggregatorClient = new AggregatorClient(aggregatorUrl);
    this.client = new StateTransitionClient(this.aggregatorClient);
    this.trustBase = RootTrustBase.fromJson(
        new String(getClass().getResourceAsStream("/trust-base.json").readAllBytes())
    );
  }

  @Test
  void testGetBlockHeight() throws Exception {
    Long blockHeight = aggregatorClient.getBlockHeight().get();
    assertNotNull(blockHeight);
    assertTrue(blockHeight > 0);
  }
}