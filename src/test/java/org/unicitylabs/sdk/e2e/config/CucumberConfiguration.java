package com.unicity.sdk.e2e.config;

import com.unicity.sdk.e2e.context.TestContext;
import io.cucumber.java.Before;
import io.cucumber.java.After;

/**
 * Cucumber configuration for dependency injection and test lifecycle management.
 * This ensures that TestContext is properly shared across all step definition classes.
 */
public class CucumberConfiguration {

    private static TestContext testContext = new TestContext();

    /**
     * Provides a shared TestContext instance for all step definition classes.
     * This method will be called by step definition classes to get
     * the shared TestContext instance.
     */
    public static TestContext getTestContext() {
        return testContext;
    }

    /**
     * Hook that runs before each scenario to reset the test context.
     * This ensures each scenario starts with a clean state.
     */
    @Before
    public void setUp() {
        testContext.clearTestState(); // Clear test state but keep clients if they exist
        System.out.println("Test context cleared for new scenario");
    }

    /**
     * Hook that runs after each scenario for cleanup.
     * This can be used for any additional cleanup if needed.
     */
    @After
    public void tearDown() {
        // Optional: Add any cleanup logic here
        // For now, we keep the context alive for potential debugging
        System.out.println("Scenario completed");
    }

    /**
     * Hook that runs after scenarios tagged with @reset to completely reset the context.
     */
    @After("@reset")
    public void fullReset() {
        testContext.reset();
        System.out.println("Full context reset performed");
    }
}