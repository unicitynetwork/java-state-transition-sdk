package org.unicitylabs.sdk;

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.jsonrpc.JsonRpcNetworkError;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestApiKeyIntegration {

    private static final String TEST_API_KEY = "test-api-key-12345";
    
    private MockAggregatorServer mockServer;
    private AggregatorClient clientWithApiKey;
    private AggregatorClient clientWithoutApiKey;

    private DataHash transactionHash;
    private RequestId requestId;
    private Authenticator authenticator;
    
    @BeforeEach
    void setUp() throws Exception {
        mockServer = new MockAggregatorServer();
        mockServer.setExpectedApiKey(TEST_API_KEY);
        mockServer.start();

        clientWithApiKey = new AggregatorClient(mockServer.getUrl(), TEST_API_KEY);
        clientWithoutApiKey = new AggregatorClient(mockServer.getUrl());

        SigningService signingService = new SigningService(
                HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));

        DataHash stateHash = new DataHash(HashAlgorithm.SHA256, HexConverter.decode("fedcba0987654321fedcba0987654321fedcba0987654321fedcba0987654321"));
        requestId = RequestId.create(signingService.getPublicKey(), stateHash);
        transactionHash = new DataHash(HashAlgorithm.SHA256, HexConverter.decode("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890"));

        authenticator = Authenticator.create(signingService, transactionHash, stateHash);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        mockServer.shutdown();
    }
    
    @Test
    public void testSubmitCommitmentWithApiKey() throws Exception {
        CompletableFuture<SubmitCommitmentResponse> future =
            clientWithApiKey.submitCommitment(requestId, transactionHash, authenticator);
        
        SubmitCommitmentResponse response = future.get(5, TimeUnit.SECONDS);
        assertEquals(SubmitCommitmentStatus.SUCCESS, response.getStatus());
        
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("Bearer " + TEST_API_KEY, request.getHeader("Authorization"));
    }
    
    @Test
    public void testSubmitCommitmentWithoutApiKeyThrowsUnauthorized() throws Exception {
        CompletableFuture<SubmitCommitmentResponse> future =
            clientWithoutApiKey.submitCommitment(requestId, transactionHash, authenticator);
        
        try {
            future.get(5, TimeUnit.SECONDS);
            fail("Expected UnauthorizedException to be thrown");
        } catch (Exception e) {
            assertInstanceOf(ExecutionException.class, e);
            assertInstanceOf(JsonRpcNetworkError.class, e.getCause());
            assertEquals("Network error [401] occurred: Unauthorized", e.getCause().getMessage());
        }
        
        RecordedRequest request = mockServer.takeRequest();
        assertNull(request.getHeader("Authorization"));
    }
    
    @Test
    public void testSubmitCommitmentWithWrongApiKeyThrowsUnauthorized() throws Exception {
        mockServer.setExpectedApiKey("different-api-key");
        
        CompletableFuture<SubmitCommitmentResponse> future =
            clientWithApiKey.submitCommitment(requestId, transactionHash, authenticator);
        
        try {
            future.get(5, TimeUnit.SECONDS);
            fail("Expected UnauthorizedException to be thrown");
        } catch (Exception e) {
            assertInstanceOf(ExecutionException.class, e);
            assertInstanceOf(JsonRpcNetworkError.class, e.getCause());
            assertEquals("Network error [401] occurred: Unauthorized",  e.getCause().getMessage());
        }
        
        RecordedRequest request = mockServer.takeRequest();
        assertEquals("Bearer " + TEST_API_KEY, request.getHeader("Authorization"));
    }
    
    @Test 
    public void testRateLimitExceeded() throws Exception {
        mockServer.simulateRateLimitForNextRequest(30);
        
        CompletableFuture<SubmitCommitmentResponse> future = 
            clientWithApiKey.submitCommitment(requestId, transactionHash, authenticator);
        
        try {
            future.get(5, TimeUnit.SECONDS);
            fail("Expected RateLimitExceededException to be thrown");
        } catch (Exception e) {
            assertInstanceOf(ExecutionException.class, e);
            assertInstanceOf(JsonRpcNetworkError.class, e.getCause());
            assertTrue(e.getCause().getMessage().contains("Network error [429] occurred: Too Many Requests"), e.getCause().getMessage());
        }
    }
    
    @Test
    public void testGetBlockHeightWorksWithoutApiKey() throws Exception {
        CompletableFuture<Long> future = clientWithoutApiKey.getBlockHeight();
        
        Long blockHeight = future.get(5, TimeUnit.SECONDS);
        assertNotNull(blockHeight);
        assertEquals(67890L, blockHeight);
    }
    
    @Test
    public void testGetBlockHeightAlsoWorksWithApiKey() throws Exception {
        CompletableFuture<Long> future = clientWithApiKey.getBlockHeight();
        
        Long blockHeight = future.get(5, TimeUnit.SECONDS);
        assertNotNull(blockHeight);
        assertEquals(67890L, blockHeight);
    }
}