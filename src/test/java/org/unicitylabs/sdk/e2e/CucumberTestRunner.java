package org.unicitylabs.sdk.e2e;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Updated Cucumber test runner configuration for E2E tests.
 * This class configures the test execution environment and feature discovery
 * with the new shared step definitions approach.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "org.unicitylabs.sdk.e2e.steps,org.unicitylabs.sdk.e2e.steps.shared,org.unicitylabs.sdk.e2e.config")
@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty,html:target/cucumber-reports,json:target/cucumber-reports/Cucumber.json,junit:target/cucumber-reports/Cucumber.xml")
@ConfigurationParameter(key = Constants.FILTER_TAGS_PROPERTY_NAME, value = "not @ignore")
@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class CucumberTestRunner {
    // This class serves as a configuration holder for Cucumber tests
    // The actual test execution is driven by the annotations above

    // Key improvements in this runner:
    // 1. Updated glue packages to include both regular and shared step definitions
    // 2. Added execution configuration parameters
    // 3. Improved plugin configuration for better reporting
}