package com.unicity.sdk.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.Duration;

/**
 * End-to-end tests for token state transitions using a deployed aggregator.
 * Tests the Java SDK against a real aggregator instance.
 * 
 * Set AGGREGATOR_URL environment variable to run these tests.
 * Example: AGGREGATOR_URL=https://gateway-test.unicity.network ./gradlew integrationTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class TokenE2ETest extends BaseTokenTest {
    
    private String aggregatorUrl;
    
    @BeforeAll
    void setUp() throws Exception {
        aggregatorUrl = System.getenv("AGGREGATOR_URL");
        if (aggregatorUrl == null || aggregatorUrl.trim().isEmpty()) {
            throw new IllegalStateException("AGGREGATOR_URL environment variable must be set");
        }
        
        // Remove trailing slash if present
        if (aggregatorUrl.endsWith("/")) {
            aggregatorUrl = aggregatorUrl.substring(0, aggregatorUrl.length() - 1);
        }
        
        logger.info("Using deployed aggregator at: {}", aggregatorUrl);
        
        // Initialize client through base class
        initializeClient();
    }
    
    @Override
    protected String getAggregatorUrl() {
        return aggregatorUrl;
    }
    
    @Override
    protected Duration getInclusionProofTimeout() {
        // Use longer timeout for real aggregator
        return Duration.ofSeconds(30);
    }
}