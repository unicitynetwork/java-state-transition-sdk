package org.unicitylabs.sdk.util;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.transaction.Commitment;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;

/**
 * Utility class for working with inclusion proofs.
 */
public class InclusionProofUtils {

  private static final Logger logger = LoggerFactory.getLogger(InclusionProofUtils.class);
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(
      30);  // 30 seconds should be enough for direct leader
  private static final Duration DEFAULT_INTERVAL = Duration.ofMillis(1000);

  private InclusionProofUtils() {
  }

  /**
   * Wait for an inclusion proof to be available and verified.
   *
   * @param client     State transition client
   * @param trustBase  Root trust base
   * @param commitment Inclusion proof commitment to wait for
   * @return Completable future with inclusion proof
   */
  public static CompletableFuture<InclusionProof> waitInclusionProof(
      StateTransitionClient client,
      RootTrustBase trustBase,
      Commitment<?> commitment
  ) {
    return waitInclusionProof(client, trustBase, commitment, DEFAULT_TIMEOUT, DEFAULT_INTERVAL);
  }

  /**
   * Wait for an inclusion proof to be available and verified with custom timeout.
   *
   * @param client     State transition client
   * @param trustBase  Root trust base
   * @param commitment Inclusion proof commitment to wait for
   * @param timeout    Maximum duration to wait for the inclusion proof
   * @param interval   Interval between checks for the inclusion proof
   * @return Completable future with inclusion proof
   */
  public static CompletableFuture<InclusionProof> waitInclusionProof(
      StateTransitionClient client,
      RootTrustBase trustBase,
      Commitment<?> commitment,
      Duration timeout,
      Duration interval
  ) {

    CompletableFuture<InclusionProof> future = new CompletableFuture<>();

    long startTime = System.currentTimeMillis();
    long timeoutMillis = timeout.toMillis();

    checkInclusionProof(client, trustBase, commitment, future, startTime, timeoutMillis,
        interval.toMillis());

    return future;
  }

  private static void checkInclusionProof(
      StateTransitionClient client,
      RootTrustBase trustBase,
      Commitment<?> commitment,
      CompletableFuture<InclusionProof> future,
      long startTime,
      long timeoutMillis,
      long intervalMillis) {

    if (System.currentTimeMillis() - startTime > timeoutMillis) {
      future.completeExceptionally(new TimeoutException("Timeout waiting for inclusion proof"));
    }

    client.getInclusionProof(commitment).thenAccept(response -> {
      InclusionProofVerificationStatus status = response.getInclusionProof()
          .verify(commitment.getRequestId(), trustBase);
      if (status == InclusionProofVerificationStatus.OK) {
        future.complete(response.getInclusionProof());
      }

      if (status == InclusionProofVerificationStatus.PATH_NOT_INCLUDED) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
          checkInclusionProof(client, trustBase, commitment, future, startTime,
              timeoutMillis,
              intervalMillis);
          scheduler.shutdown();
        }, intervalMillis, TimeUnit.MILLISECONDS);
      } else {
        future.completeExceptionally(
            new RuntimeException(String.format("Inclusion proof verification failed: %s", status)));
      }
    }).exceptionally(e -> {
      future.completeExceptionally(e);
      return null;
    });
  }
}