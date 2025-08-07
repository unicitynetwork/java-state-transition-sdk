package com.unicity.sdk.e2e;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests for token operations using CommonTestFlow. Matches TypeScript SDK's test
 * structure.
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class TokenE2ETest {

  private AggregatorClient aggregatorClient;
  private StateTransitionClient client;

  @BeforeEach
  void setUp() {
    String aggregatorUrl = System.getenv("AGGREGATOR_URL");
    assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

    aggregatorClient = new AggregatorClient(aggregatorUrl);
    client = new StateTransitionClient(aggregatorClient);
  }

  @Test
  void testGetBlockHeight() throws Exception {
    Long blockHeight = aggregatorClient.getBlockHeight().get();
    assertNotNull(blockHeight);
    assertTrue(blockHeight > 0);
  }

  @Test
  void testTransferFlow() throws Exception {
    CommonTestFlow.testTransferFlow(client);
  }
//
//    @Test
//    void testOfflineTransferFlow() throws Exception {
//        CommonTestFlow.testOfflineTransferFlow(client);
//    }

  // Token splitting will be added once implemented
  // @Test
  // void testSplitFlow() throws Exception {
  //     CommonTestFlow.testSplitFlow(client);
  // }
}