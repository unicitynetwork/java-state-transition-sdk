package com.unicity.sdk.e2e;

import com.unicity.sdk.api.AggregatorClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic end-to-end test to verify connectivity with aggregator.
 */
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class BasicE2ETest {

    @Test
    void testVerifyBlockHeight() throws Exception {
        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");
        
        AggregatorClient aggregatorClient = new AggregatorClient(aggregatorUrl);
        Long blockHeight = aggregatorClient.getBlockHeight().get();
        
        System.out.println("block height: " + blockHeight);
        assertNotNull(blockHeight);
        assertTrue(blockHeight > 0);
    }
}