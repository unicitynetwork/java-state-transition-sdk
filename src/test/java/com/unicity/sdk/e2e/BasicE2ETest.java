package com.unicity.sdk.e2e;

import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

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

    @Test
    void testCommitmentPerformance() throws Exception {
        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

        AggregatorClient aggregatorClient = new AggregatorClient(aggregatorUrl);

        long startTime = System.currentTimeMillis();
        var sr = new SecureRandom();
        byte[] randomSecret = new byte[32];
        sr.nextBytes(randomSecret);
        byte[] stateBytes = new byte[32];
        sr.nextBytes(stateBytes);
        var stateHash = DataHasher.digest(HashAlgorithm.SHA256, stateBytes);
        var txDataHash = DataHasher.digest(HashAlgorithm.SHA256, "test commitment performance".getBytes());
        var res = SigningService.createFromSecret(randomSecret, null).thenCompose(
          ss -> {
              var req = RequestId.createFromImprint(ss.getPublicKey(), stateHash.getImprint());
              var auth = Authenticator.create(ss, txDataHash, stateHash);
              return req.thenCombine(auth, (requestId, authenticator) ->
                      aggregatorClient.submitTransaction(
                              requestId,
                              txDataHash,
                              authenticator
                      )).thenCompose(Function.identity());
          }
        ).get();
        if (res.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            System.err.println("Commitment submission failed with status: " + res.getStatus());
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("Commitment submission took: " + duration + " ms");

        assertTrue(duration < 5000, "Commitment submission should take less than 5 seconds");
    }

    @Test
    void testCommitmentPerformanceMultiThreaded() throws Exception {
        int threadCount = 100; // configure as needed
        int commitmentsPerThread = 10; // configure as needed

        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

        AggregatorClient aggregatorClient = new AggregatorClient(aggregatorUrl);
        ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(threadCount * commitmentsPerThread);

        long startTime = System.currentTimeMillis();
        java.util.List<java.util.concurrent.Future<Boolean>> results = new java.util.ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            for (int c = 0; c < commitmentsPerThread; c++) {
                results.add(executor.submit(() -> {
                    try {
                        var sr = new SecureRandom();
                        byte[] randomSecret = new byte[32];
                        sr.nextBytes(randomSecret);
                        byte[] stateBytes = new byte[32];
                        sr.nextBytes(stateBytes);
                        byte[] txData = new byte[32];
                        sr.nextBytes(txData);
                        var stateHash = DataHasher.digest(HashAlgorithm.SHA256, stateBytes);
                        var txDataHash = DataHasher.digest(HashAlgorithm.SHA256, txData);
                        var res = SigningService.createFromSecret(randomSecret, null).thenCompose(
                                ss -> {
                                    var req = RequestId.createFromImprint(ss.getPublicKey(), stateHash.getImprint());
                                    var auth = Authenticator.create(ss, txDataHash, stateHash);
                                    return req.thenCombine(auth, (requestId, authenticator) ->
                                            aggregatorClient.submitTransaction(
                                                    requestId,
                                                    txDataHash,
                                                    authenticator
                                            )).thenCompose(Function.identity());
                                }
                        ).get();
                        return res.getStatus() == SubmitCommitmentStatus.SUCCESS;
                    } finally {
                        latch.countDown();
                    }
                }));
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        long duration = endTime - startTime;
        long successCount = results.stream().filter(f -> {
            try { return f.get(); } catch (Exception e) { return false; }
        }).count();

        System.out.println("Total commitments: " + (threadCount * commitmentsPerThread));
        System.out.println("Successful: " + successCount);
        System.out.println("Commitment submission took: " + duration + " ms");

        assertEquals(threadCount * commitmentsPerThread, successCount, "All commitments should succeed");
    }
}
