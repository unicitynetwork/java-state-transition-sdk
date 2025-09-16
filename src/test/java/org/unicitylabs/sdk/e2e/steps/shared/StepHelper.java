package org.unicitylabs.sdk.e2e.steps.shared;

import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.e2e.config.CucumberConfiguration;
import org.unicitylabs.sdk.e2e.context.TestContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public void finalizeTransfer(String username, Token <?> token, Transaction<TransferTransactionData> tx) throws Exception {

        byte[] secret = context.getUserSecret().get(username);

        Token <?> currentNameTagToken = context.getNameTagToken(username);
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
}
