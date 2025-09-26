package org.unicitylabs.sdk.e2e;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.common.BaseEscrowSwapTest;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;


@Tag("integration")
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class E2EEscrowSwapTest extends BaseEscrowSwapTest {
  @BeforeEach
  void setUp() throws IOException {
    String aggregatorUrl = System.getenv("AGGREGATOR_URL");
    Assertions.assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

    this.client = new StateTransitionClient(new AggregatorClient(aggregatorUrl));
    this.trustBase = UnicityObjectMapper.JSON.readValue(
        getClass().getResourceAsStream("/trust-base.json"),
        RootTrustBase.class
    );
  }
}
