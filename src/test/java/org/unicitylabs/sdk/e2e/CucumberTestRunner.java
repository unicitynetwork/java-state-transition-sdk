package org.unicitylabs.sdk.e2e;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

/**
 * Updated Cucumber test runner configuration for E2E tests.
 * This class configures the test execution environment and feature discovery
 * with the new shared step definitions approach.
 */
//@Suite
//@IncludeEngines("cucumber")
//@SelectPackages("org.unicitylabs.sdk.features")
//@ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "org.unicitylabs.sdk.e2e.steps,org.unicitylabs.sdk.e2e.steps.shared,org.unicitylabs.sdk.e2e.config")
//@ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty,html:build/cucumber-reports/cucumber.html,json:build/cucumber-reports/cucumber.json")
//@ConfigurationParameter(key = Constants.EXECUTION_DRY_RUN_PROPERTY_NAME, value = "false")
//@ConfigurationParameter(key = Constants.PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
//public class CucumberTestRunner {
//    static {
//        // Only set default tags if no tags are specified
//        if (System.getProperty("cucumber.filter.tags") == null) {
//            System.setProperty("cucumber.filter.tags", "not @ignore");
//        }
//    }
//}