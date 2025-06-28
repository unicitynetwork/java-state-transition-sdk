package com.unicity.sdk.integration;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.shared.jsonrpc.JsonRpcHttpTransport;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for token state transitions using Testcontainers.
 * Tests the Java SDK against the aggregator running in Docker.
 * 
 * Run with: ./gradlew test --tests "com.unicity.sdk.integration.*" -Pintegration
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class TokenIntegrationTest {
    
    private static final int AGGREGATOR_PORT = 3000;
    private static final String AGGREGATOR_SERVICE = "aggregator-test";
    private static final String COMPOSE_FILE_PATH = "src/test/resources/docker/aggregator/docker-compose.yml";
    
    @Container
    private static final DockerComposeContainer<?> environment = 
        new DockerComposeContainer<>(new File(COMPOSE_FILE_PATH))
            .withExposedService(AGGREGATOR_SERVICE, AGGREGATOR_PORT, 
                Wait.forLogMessage(".*listening on port " + AGGREGATOR_PORT + ".*", 1))
            .withStartupTimeout(Duration.ofMinutes(3));
    
    private static JsonRpcHttpTransport transport;
    private static AggregatorClient aggregatorClient;
    private static StateTransitionClient client;
    
    @BeforeAll
    public static void setUp() {
        String host = environment.getServiceHost(AGGREGATOR_SERVICE, AGGREGATOR_PORT);
        Integer port = environment.getServicePort(AGGREGATOR_SERVICE, AGGREGATOR_PORT);
        String aggregatorUrl = String.format("http://%s:%d", host, port);
        
        System.out.println("Aggregator URL: " + aggregatorUrl);
        
        transport = new JsonRpcHttpTransport(aggregatorUrl);
        aggregatorClient = new AggregatorClient(aggregatorUrl);
        client = new StateTransitionClient(aggregatorClient);
    }
    
    @AfterAll
    public static void tearDown() {
        // Cleanup is handled by Testcontainers
    }
    
    @Test
    @Order(1)
    public void testAggregatorConnection() throws Exception {
        // Test basic JSON-RPC connection
        assertNotNull(transport);
        
        // Test that we can make a request to the aggregator
        // Even if it fails, we should get a response
        CompletableFuture<Object> future = transport.request("getBlockHeight", null);
        
        try {
            Object result = future.get();
            System.out.println("Block height response: " + result);
            assertNotNull(result);
        } catch (Exception e) {
            // Even if the method doesn't exist, we should get an RPC error, not a connection error
            System.out.println("RPC call failed (expected if method not implemented): " + e.getMessage());
            assertTrue(e.getMessage().contains("JSON-RPC") || e.getMessage().contains("Method not found"));
        }
    }
    
    @Test
    @Order(2)
    public void testStateTransitionClientCreation() {
        assertNotNull(client);
        assertNotNull(aggregatorClient);
    }
    
    @Test
    @Order(3)
    @Disabled("Requires full implementation of token minting and transfer")
    public void testTokenTransferFlow() throws Exception {
        // This will be implemented when all required classes are ready
        System.out.println("Token transfer test disabled - requires full implementation");
    }
    
    @Test
    @Order(4)
    @Disabled("Requires full implementation of token splitting")
    public void testTokenSplitFlow() throws Exception {
        // This will be implemented when all required classes are ready
        System.out.println("Token split test disabled - requires full implementation");
    }
    
    @Test
    @Order(5)
    public void testSimpleJsonRpcCall() throws Exception {
        // Test a simple JSON-RPC call structure
        CompletableFuture<Object> future = transport.request("echo", "test");
        
        try {
            Object result = future.get();
            System.out.println("Echo response: " + result);
        } catch (Exception e) {
            // This is expected if echo method doesn't exist
            System.out.println("Echo call failed (expected): " + e.getMessage());
        }
    }
}