package com.unicity.sdk.integration;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration tests for token state transitions using Testcontainers.
 * Tests the Java SDK against the aggregator running in Docker.
 * 
 * Run with: ./gradlew integrationTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class TokenIntegrationTest extends BaseTokenTest {
    
    private Network network;
    private GenericContainer<?> mongo1;
    private GenericContainer<?> mongo2;
    private GenericContainer<?> mongo3;
    private GenericContainer<?> mongoSetup;
    private GenericContainer<?> aggregator;
    
    @BeforeAll
    void setUp() throws Exception {
        network = Network.newNetwork();
        
        // Start MongoDB replica set
        mongo1 = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withNetworkAliases("mongo1")
                .withCommand("--replSet", "rs0", "--bind_ip_all")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        mongo2 = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withNetworkAliases("mongo2")
                .withCommand("--replSet", "rs0", "--bind_ip_all")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        mongo3 = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withNetworkAliases("mongo3")
                .withCommand("--replSet", "rs0", "--bind_ip_all")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        
        mongo1.start();
        mongo2.start();
        mongo3.start();
        
        // Wait for MongoDB to be ready
        Thread.sleep(5000);
        
        // Initialize replica set
        mongoSetup = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withCopyFileToContainer(
                        org.testcontainers.utility.MountableFile.forClasspathResource("docker/aggregator/mongo-init.js"),
                        "/mongo-init.js")
                .withCommand("mongosh", "--host", "mongo1:27017", "--file", "/mongo-init.js");
        
        mongoSetup.start();
        
        // Start aggregator
        aggregator = new GenericContainer<>(DockerImageName.parse("ghcr.io/unicitynetwork/aggregators_net:bbabb5f093e829fa789ed6e83f57af98df3f1752"))
                .withNetwork(network)
                .withNetworkAliases("aggregator-test")
                .withExposedPorts(3000)
                .withEnv("MONGODB_URI", "mongodb://mongo1:27017")
                .withEnv("USE_MOCK_ALPHABILL", "true")
                .withEnv("ALPHABILL_PRIVATE_KEY", "FF00000000000000000000000000000000000000000000000000000000000000")
                .withEnv("DISABLE_HIGH_AVAILABILITY", "true")
                .withEnv("PORT", "3000")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        aggregator.start();
        
        // Initialize client through base class
        initializeClient();
    }
    
    @AfterAll
    void tearDown() {
        if (aggregator != null) aggregator.stop();
        if (mongoSetup != null) mongoSetup.stop();
        if (mongo1 != null) mongo1.stop();
        if (mongo2 != null) mongo2.stop();
        if (mongo3 != null) mongo3.stop();
        if (network != null) network.close();
    }
    
    @Override
    protected String getAggregatorUrl() {
        Integer mappedPort = aggregator.getMappedPort(3000);
        return "http://localhost:" + mappedPort;
    }
    
    @Test
    @Order(2)
    @Override
    void testGetBlockHeight() throws Exception {
        Thread.sleep(2000);  // Wait for aggregator to stabilize
        super.testGetBlockHeight();
    }
}