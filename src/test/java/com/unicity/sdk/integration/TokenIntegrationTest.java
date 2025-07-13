package com.unicity.sdk.integration;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.e2e.CommonTestFlow;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for token state transitions using Testcontainers.
 * Tests the Java SDK against the aggregator running in Docker.
 * 
 * Run with: ./gradlew integrationTest
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class TokenIntegrationTest {
    
    private Network network;
    private GenericContainer<?> mongo1;
    private GenericContainer<?> mongo2;
    private GenericContainer<?> mongo3;
    private GenericContainer<?> mongoSetup;
    private GenericContainer<?> aggregator;
    
    private AggregatorClient aggregatorClient;
    private StateTransitionClient client;
    
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
        
        // Setup replica set
        mongoSetup = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withCommand("mongosh", "--host", "mongo1:27017", "--eval",
                        "rs.initiate({_id:'rs0',members:[{_id:0,host:'mongo1:27017'},{_id:1,host:'mongo2:27017'},{_id:2,host:'mongo3:27017'}]})")
                .withStartupCheckStrategy(new org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy())
                .dependsOn(mongo1, mongo2, mongo3);
        mongoSetup.start();
        
        // Start aggregator
        aggregator = new GenericContainer<>(
                DockerImageName.parse("ghcr.io/unicitylabs/unicity-aggregator-node-js:v1.1.0"))
                .withNetwork(network)
                .withNetworkAliases("aggregator")
                .withExposedPorts(8080)
                .withEnv("MONGODB_URL", "mongodb://mongo1:27017,mongo2:27017,mongo3:27017/test?replicaSet=rs0")
                .withEnv("PORT", "8080")
                .withEnv("IS_LEADER", "true")
                .waitingFor(Wait.forHttp("/status")
                        .forPort(8080)
                        .withStartupTimeout(java.time.Duration.ofMinutes(2)))
                .dependsOn(mongoSetup);
        aggregator.start();
        
        initializeClient();
    }
    
    private void initializeClient() {
        String aggregatorUrl = String.format("http://localhost:%d", aggregator.getMappedPort(8080));
        aggregatorClient = new AggregatorClient(aggregatorUrl);
        client = new StateTransitionClient(aggregatorClient);
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
    
    @Test
    @Order(1)
    void testAggregatorIsRunning() {
        assertTrue(aggregator.isRunning());
    }
    
    @Test
    @Order(2)
    void testGetBlockHeight() throws Exception {
        Long blockHeight = aggregatorClient.getBlockHeight().get();
        assertNotNull(blockHeight);
        assertTrue(blockHeight >= 0);
    }
    
    @Test
    @Order(3) 
    void testTransferFlow() throws Exception {
        CommonTestFlow.testTransferFlow(client);
    }
    
    @Test
    @Order(4)
    void testOfflineTransferFlow() throws Exception {
        CommonTestFlow.testOfflineTransferFlow(client);
    }
}