package org.unicitylabs.sdk.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.CertificationResponse;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.api.JsonRpcAggregatorClient;
import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.signing.SigningService;

/**
 * Basic end-to-end test to verify connectivity with aggregator.
 */
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
@Disabled("Skip performance tests")
public class BasicE2ETest {

    @Test
    void testCommitmentPerformance() throws Exception {
        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");

        JsonRpcAggregatorClient aggregatorClient = new JsonRpcAggregatorClient(aggregatorUrl);

        long startTime = System.currentTimeMillis();
        SecureRandom sr = new SecureRandom();
        byte[] randomSecret = new byte[32];
        sr.nextBytes(randomSecret);
        byte[] stateBytes = new byte[32];
        sr.nextBytes(stateBytes);
        DataHash stateHash = new DataHasher(HashAlgorithm.SHA256).update(stateBytes).digest();
        DataHash txDataHash = new DataHasher(HashAlgorithm.SHA256).update("test commitment performance".getBytes()).digest();
        SigningService signingService = SigningService.createFromSecret(randomSecret);
        CertificationData certificationData = CertificationData.create(stateHash, txDataHash, signingService);
        CertificationResponse response = aggregatorClient.submitCertificationRequest(certificationData, false).get();

        if (response.getStatus() != CertificationStatus.SUCCESS) {
            System.err.println("Commitment submission failed with status: " + response.getStatus());
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

        JsonRpcAggregatorClient aggregatorClient = new JsonRpcAggregatorClient(aggregatorUrl);
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

                        DataHash stateHash = new DataHasher(HashAlgorithm.SHA256).update(stateBytes).digest();
                        DataHash txDataHash = new DataHasher(HashAlgorithm.SHA256).update(txData).digest();
                        SigningService signingService = SigningService.createFromSecret(randomSecret);
                        StateId stateId = StateId.create(signingService.getPublicKey(), stateHash.getImprint());
                        CertificationData certificationData = CertificationData.create(stateId, txDataHash, signingService);
                        CertificationResponse response = aggregatorClient.submitCertificationRequest(certificationData, false).get();
                        return response.getStatus() == CertificationStatus.SUCCESS;
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
