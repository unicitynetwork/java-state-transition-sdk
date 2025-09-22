package org.unicitylabs.sdk.e2e.steps.shared;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.e2e.config.CucumberConfiguration;
import org.unicitylabs.sdk.e2e.context.TestContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
import org.unicitylabs.sdk.predicate.UnmaskedPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.*;
import org.unicitylabs.sdk.utils.TestUtils;
import org.unicitylabs.sdk.utils.helpers.CommitmentResult;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.unicitylabs.sdk.util.InclusionProofUtils.waitInclusionProof;
import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;


public class StepHelper {

    private final TestContext context;

    public StepHelper() {  // ✅ Public zero-argument constructor
        this.context = CucumberConfiguration.getTestContext();
    }

    public Token createNameTagTokenForUser(String userName, TokenType type, String nametag, String nametagData) throws Exception {
        byte[] nametagNonce = TestUtils.generateRandomBytes(32);

        MaskedPredicate nametagPredicate = MaskedPredicate.create(
                SigningService.createFromMaskedSecret(context.getUserSecret().get(userName), nametagNonce),
                HashAlgorithm.SHA256,
                nametagNonce
        );

        TokenType nametagTokenType = TestUtils.generateRandomTokenType();
        DirectAddress nametagAddress = nametagPredicate.getReference(nametagTokenType).toAddress();

        DirectAddress userAddress = UnmaskedPredicateReference.create(
                nametagTokenType,
                SigningService.createFromSecret(context.getUserSecret().get(userName)),
                HashAlgorithm.SHA256
        ).toAddress();

        var nametagMintCommitment = org.unicitylabs.sdk.transaction.MintCommitment.create(
                new NametagMintTransactionData<>(
                        nametag,
                        nametagTokenType,
                        nametagAddress,
                        TestUtils.generateRandomBytes(32),
                        userAddress
                )
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(nametagMintCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit nametag mint commitment: " + response.getStatus());
        }

        InclusionProof inclusionProof = waitInclusionProof(context.getClient(), nametagMintCommitment).get();
        Transaction<? extends MintTransactionData<?>> nametagGenesis = nametagMintCommitment.toTransaction(inclusionProof);

        return new Token(
                new org.unicitylabs.sdk.token.TokenState(nametagPredicate, null),
                nametagGenesis
        );
    }

    public void transferToken(String fromUser, String toUser, Token token, Address toAddress, String customData) throws Exception {
        SigningService fromSigningService = context.getUserSigningServices().get(fromUser);

        // Create data hash and state data if custom data provided
        DataHash dataHash = null;
        byte[] stateData = null;
        if (customData != null && !customData.isEmpty()) {
            stateData = customData.getBytes(StandardCharsets.UTF_8);
            dataHash = TestUtils.hashData(stateData);
        }

        // Submit transfer commitment
        TransferCommitment transferCommitment = TransferCommitment.create(
                token,
                toAddress,
                randomBytes(32),
                dataHash,
                null,
                fromSigningService
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(token, transferCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit transfer commitment: " + response.getStatus());
        }

        // Wait for inclusion proof
        InclusionProof inclusionProof = waitInclusionProof(
                context.getClient(),
                transferCommitment
        ).get();
        Transaction<TransferTransactionData> transferTransaction = transferCommitment.toTransaction(
                token,
                inclusionProof
        );

        context.savePendingTransfer(toUser, token, transferTransaction);
    }

    public void finalizeTransfer(String username, Token<?> token, Transaction<TransferTransactionData> tx) throws Exception {

        byte[] secret = context.getUserSecret().get(username);

        Token<?> currentNameTagToken = context.getNameTagToken(username);
        List<Token> nametagTokens = context.getNameTagTokens().get(username);
        if (nametagTokens != null && !nametagTokens.isEmpty()) {
            for (Token<?> t : nametagTokens) {
                String actualNametagAddress = tx.getData().getRecipient().getAddress();
                String expectedProxyAddress = ProxyAddress.create(t.getId()).getAddress();

                if (actualNametagAddress.equalsIgnoreCase(expectedProxyAddress)) {
                    currentNameTagToken = t;
                    break;
                }
            }
        }

        List<Token<?>> additionalTokens = new ArrayList<>();
        if (currentNameTagToken != null) {
            additionalTokens.add(currentNameTagToken);
        }

        Predicate unlockPredicate = context.getUserPredicate().get(username);
        if (unlockPredicate == null){
            context.getUserSigningServices().put(username, SigningService.createFromSecret(secret));
            unlockPredicate = UnmaskedPredicate.create(
                        context.getUserSigningServices().get(username),
                        HashAlgorithm.SHA256,
                        tx.getData().getSalt()
                );
        }

        TokenState recipientState = new TokenState(
                unlockPredicate,
                null
        );

        Token finalizedToken = context.getClient().finalizeTransaction(
                token,
                recipientState,
                tx,
                additionalTokens
        );

        context.addUserToken(username, finalizedToken);
    }

    public boolean submitSingleCommitment() {
        try {
            byte[] randomSecret = TestUtils.generateRandomBytes(32);
            byte[] stateBytes = TestUtils.generateRandomBytes(32);
            byte[] txData = TestUtils.generateRandomBytes(32);

            DataHash stateHash = TestUtils.hashData(stateBytes);
            DataHash txDataHash = TestUtils.hashData(txData);
            SigningService signingService = SigningService.createFromSecret(randomSecret);
            var requestId = TestUtils.createRequestId(signingService, stateHash);
            var authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);

            SubmitCommitmentResponse response = context.getAggregatorClient()
                    .submitCommitment(requestId, txDataHash, authenticator).get();
            return response.getStatus() == SubmitCommitmentStatus.SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }

    public void verifyAllInclusionProofsInParallel(int timeoutSeconds)
            throws InterruptedException {
        List<CommitmentResult> results = collectCommitmentResults();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CountDownLatch latch = new CountDownLatch(results.size());

        long startAll = System.nanoTime();
        long globalTimeout = startAll + TimeUnit.SECONDS.toNanos(timeoutSeconds);

        for (CommitmentResult result : results) {
            executor.submit(() -> {
                long inclStart = System.nanoTime();
                boolean verified = false;
                String errorMessage = "Global timeout reached";

                try {
                    while (System.nanoTime() < globalTimeout && !verified) {
                        try {
                            InclusionProof proof = context.getAggregatorClient()
                                    .getInclusionProof(result.getRequestId())
                                    .get(calculateRemainingTimeout(globalTimeout), TimeUnit.MILLISECONDS);

                            if (proof != null && proof.verify(result.getRequestId())
                                    == InclusionProofVerificationStatus.OK) {
                                result.markVerified(inclStart, System.nanoTime());
                                verified = true;
                            } else {
                                // Неуспешная верификация, но продолжаем пытаться
                                InclusionProofVerificationStatus status = proof.verify(result.getRequestId());
                                errorMessage = status.toString();
                                Thread.sleep(1000); // Небольшая пауза перед повторной попыткой
                            }
                        } catch (TimeoutException e) {
                            // Таймаут отдельной операции, продолжаем цикл
                            errorMessage = "Individual operation timeout: " + e.getMessage();
                        } catch (ExecutionException e) {
                            // Ошибка выполнения, продолжаем цикл
                            errorMessage = "Execution error: " + e.getMessage();
                            Thread.sleep(1000); // Пауза перед повторной попыткой
                        }
                    }

                    if (!verified) {
                        result.markFailedVerification(inclStart, System.nanoTime(), errorMessage);
                    }

                } catch (Exception e) {
                    result.markFailedVerification(inclStart, System.nanoTime(),
                            "Unexpected error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all tasks to complete or timeout
        boolean finished = latch.await(timeoutSeconds, TimeUnit.SECONDS);
        executor.shutdownNow();

        long endAll = System.nanoTime();
        System.out.println("All inclusion proofs completed in: " + ((endAll - startAll) / 1_000_000) + " ms");

        if (!finished) {
            System.err.println("Timeout reached before all inclusion proofs were verified");
        }
    }

    private long calculateRemainingTimeout(long globalTimeoutNanos) {
        long remaining = globalTimeoutNanos - System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(Math.max(remaining, 100)); // Минимум 100мс
    }

    public List<CommitmentResult> collectCommitmentResults() {
        return context.getCommitmentFutures().stream()
                .map(f -> {
                    try {
                        return f.get(); // wait for completion
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Helper method to extract aggregator info from username
    public String extractAggregatorFromUserName(String userName) {
        if (userName.contains("-Aggregator")) {
            return userName.substring(userName.indexOf("-Aggregator"));
        }
        return "Unknown-Aggregator";
    }

    // Updated helper method for your existing CommitmentResult class
    public void verifyAllInclusionProofsInParallelForMultipleAggregators(int timeoutSeconds, List<AggregatorClient> aggregatorClients) throws Exception {
        List<CommitmentResult> results = collectCommitmentResults();

        // Group results by aggregator
        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
                .collect(Collectors.groupingBy(r -> extractAggregatorFromUserName(r.getUserName())));

        ExecutorService executor = Executors.newFixedThreadPool(aggregatorClients.size());
        List<CompletableFuture<Void>> verificationFutures = new ArrayList<>();

        for (int i = 0; i < aggregatorClients.size(); i++) {
            AggregatorClient aggregatorClient = aggregatorClients.get(i);
            String aggregatorId = "Aggregator" + i;
            List<CommitmentResult> aggregatorResults = resultsByAggregator.getOrDefault("-" + aggregatorId, new ArrayList<>());

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    verifyInclusionProofsForAggregator(aggregatorClient, aggregatorResults, timeoutSeconds);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to verify inclusion proofs for " + aggregatorId, e);
                }
            }, executor);

            verificationFutures.add(future);
        }

        try {
            CompletableFuture.allOf(verificationFutures.toArray(new CompletableFuture[0]))
                    .get(timeoutSeconds + 10, TimeUnit.SECONDS); // Add buffer time for processing
        } finally {
            executor.shutdown();
        }
    }

    private void verifyInclusionProofsForAggregator(AggregatorClient aggregatorClient,
                                                    List<CommitmentResult> results,
                                                    int timeoutSeconds) throws Exception {
        long globalStartTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        for (CommitmentResult result : results) {
            long inclusionStartTime = System.nanoTime();

            if (!result.isSuccess()) {
                long inclusionEndTime = System.nanoTime();
                result.markFailedVerification(inclusionStartTime, inclusionEndTime, "Commitment submission failed");
                continue;
            }

            boolean verified = false;
            String statusMessage = "Timeout waiting for inclusion proof";

            // Poll for inclusion proof with timeout
            while (System.currentTimeMillis() - globalStartTime < timeoutMillis) {
                try {
                    // Check if inclusion proof is available
                     InclusionProof proofResponse = aggregatorClient
                            .getInclusionProof(result.getRequestId()).get(5, TimeUnit.SECONDS);
                    if (proofResponse != null && proofResponse.verify(result.getRequestId())
                            == InclusionProofVerificationStatus.OK) {
                        System.out.println("InclusionProofVerificationStatus.OK");
                        result.markVerified(inclusionStartTime, System.nanoTime());
                        verified = true;
                        break;
                    } else {
                        InclusionProofVerificationStatus status = proofResponse.verify(result.getRequestId());
                        System.out.println(status.toString());
                        statusMessage = status.toString();
                    }
                    Thread.sleep(1000);
                } catch (TimeoutException e) {
                    // Continue polling
                    statusMessage = "Timeout during proof retrieval";
                } catch (Exception e) {
                    statusMessage = "Error retrieving proof: " + e.getMessage();
                    break;
                }
            }

            long inclusionEndTime = System.nanoTime();

            // Use your existing methods to mark verification result
            if (verified) {
                result.markVerified(inclusionStartTime, inclusionEndTime);
            } else {
                result.markFailedVerification(inclusionStartTime, inclusionEndTime, statusMessage);
            }
        }
    }

    // Method to print detailed results by aggregator
    public void printDetailedResultsByAggregator(List<CommitmentResult> results, int aggregatorCount) {
        System.out.println("\n=== Detailed Results by Aggregator ===");

        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
                .collect(Collectors.groupingBy(r -> extractAggregatorFromUserName(r.getUserName())));

        for (int i = 0; i < aggregatorCount; i++) {
            String aggregatorId = "-Aggregator" + i;
            List<CommitmentResult> aggregatorResults = resultsByAggregator.getOrDefault(aggregatorId, new ArrayList<>());

            long verifiedCount = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .count();

            double successRate = aggregatorResults.isEmpty() ? 0 :
                    (double) verifiedCount / aggregatorResults.size() * 100;

            // Calculate average inclusion proof time for verified commitments
            OptionalDouble avgInclusionTime = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .mapToDouble(CommitmentResult::getInclusionDurationMillis)
                    .average();

            System.out.println("Aggregator" + i + " (localhost:" + (3000 + i * 5080) + "):");
            System.out.println("  Total commitments: " + aggregatorResults.size());
            System.out.println("  Verified: " + verifiedCount + " / " + aggregatorResults.size());
            System.out.println("  Success rate: " + String.format("%.2f%%", successRate));

            if (avgInclusionTime.isPresent()) {
                System.out.println("  Average inclusion time: " + String.format("%.2f ms", avgInclusionTime.getAsDouble()));
            }

            // Print failed verifications
            List<CommitmentResult> failed = aggregatorResults.stream()
                    .filter(r -> !r.isVerified())
                    .collect(Collectors.toList());

            if (!failed.isEmpty()) {
                System.out.println("  Failed verifications (" + failed.size() + "):");
                failed.forEach(r -> System.out.println("    ❌ " + r.getRequestId() +
                        " - " + (r.getStatus() != null ? r.getStatus() : "Unknown error")));
            } else {
                System.out.println("  ✅ All commitments verified successfully!");
            }

            System.out.println();
        }
    }

    public void printPerformanceComparison(List<CommitmentResult> results, int aggregatorCount) {
        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
                .collect(Collectors.groupingBy(r -> extractAggregatorFromUserName(r.getUserName())));

        System.out.println("=== 🏆 PERFORMANCE WINNER ANALYSIS ===");

        // Find best success rate
        double bestSuccessRate = 0;
        String bestSuccessAggregator = "";

        // Find fastest average inclusion time
        double fastestAvgTime = Double.MAX_VALUE;
        String fastestAggregator = "";

        for (int i = 0; i < aggregatorCount; i++) {
            String aggregatorId = "-Aggregator" + i;
            List<CommitmentResult> aggregatorResults = resultsByAggregator.getOrDefault(aggregatorId, new ArrayList<>());

            if (aggregatorResults.isEmpty()) continue;

            long verifiedCount = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .count();

            double successRate = (double) verifiedCount / aggregatorResults.size() * 100;

            if (successRate > bestSuccessRate) {
                bestSuccessRate = successRate;
                bestSuccessAggregator = "Aggregator" + i;
            }

            OptionalDouble avgInclusionTime = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .mapToDouble(CommitmentResult::getInclusionDurationMillis)
                    .average();

            if (avgInclusionTime.isPresent() && avgInclusionTime.getAsDouble() < fastestAvgTime) {
                fastestAvgTime = avgInclusionTime.getAsDouble();
                fastestAggregator = "Aggregator" + i;
            }
        }

        System.out.println("🥇 Highest Success Rate: " + bestSuccessAggregator +
                " (" + String.format("%.2f%%", bestSuccessRate) + ")");

        if (fastestAvgTime != Double.MAX_VALUE) {
            System.out.println("⚡ Fastest Inclusion Time: " + fastestAggregator +
                    " (" + String.format("%.2f ms", fastestAvgTime) + ")");
        }

        System.out.println("=====================================\n");
    }
}