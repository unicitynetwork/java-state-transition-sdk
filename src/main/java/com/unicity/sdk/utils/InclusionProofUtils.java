package com.unicity.sdk.utils;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for working with inclusion proofs
 */
public class InclusionProofUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(InclusionProofUtils.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(1000);
    
    /**
     * Wait for an inclusion proof to be available and verified
     */
    public static CompletableFuture<InclusionProof> waitInclusionProof(
            StateTransitionClient client,
            Commitment<?> commitment) {
        return waitInclusionProof(client, commitment, DEFAULT_TIMEOUT, DEFAULT_INTERVAL);
    }
    
    /**
     * Wait for an inclusion proof to be available and verified with custom timeout
     */
    public static CompletableFuture<InclusionProof> waitInclusionProof(
            StateTransitionClient client,
            Commitment<?> commitment,
            Duration timeout,
            Duration interval) {
        
        CompletableFuture<InclusionProof> future = new CompletableFuture<>();
        
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        
        checkInclusionProof(client, commitment, future, startTime, timeoutMillis, interval.toMillis());
        
        return future;
    }
    
    private static void checkInclusionProof(
            StateTransitionClient client,
            Commitment<?> commitment,
            CompletableFuture<InclusionProof> future,
            long startTime,
            long timeoutMillis,
            long intervalMillis) {
        
        if (System.currentTimeMillis() - startTime > timeoutMillis) {
            future.completeExceptionally(new TimeoutException("Timeout waiting for inclusion proof"));
            return;
        }
        
        client.getInclusionProof(commitment)
            .thenCompose(inclusionProof -> 
                inclusionProof.verify(commitment.getRequestId().toBigInt())
                    .thenApply(status -> new VerificationResult(inclusionProof, status))
            )
            .whenComplete((result, error) -> {
                if (error != null) {
                    // If it's a 404-like error, retry
                    if (error instanceof CompletionException && error.getCause() != null) {
                        String message = error.getCause().getMessage();
                        if (message != null && message.contains("404")) {
                            logger.debug("Inclusion proof not yet available, retrying...");
                            scheduleRetry(client, commitment, future, startTime, timeoutMillis, intervalMillis);
                            return;
                        }
                    }
                    future.completeExceptionally(error);
                } else if (result.status == InclusionProofVerificationStatus.OK) {
                    future.complete(result.inclusionProof);
                } else {
                    logger.debug("Inclusion proof verification status: {}, retrying...", result.status);
                    scheduleRetry(client, commitment, future, startTime, timeoutMillis, intervalMillis);
                }
            });
    }
    
    private static void scheduleRetry(
            StateTransitionClient client,
            Commitment<?> commitment,
            CompletableFuture<InclusionProof> future,
            long startTime,
            long timeoutMillis,
            long intervalMillis) {
        
        CompletableFuture
            .delayedExecutor(intervalMillis, TimeUnit.MILLISECONDS)
            .execute(() -> checkInclusionProof(client, commitment, future, startTime, timeoutMillis, intervalMillis));
    }
    
    private static class VerificationResult {
        final InclusionProof inclusionProof;
        final InclusionProofVerificationStatus status;
        
        VerificationResult(InclusionProof inclusionProof, InclusionProofVerificationStatus status) {
            this.inclusionProof = inclusionProof;
            this.status = status;
        }
    }
}