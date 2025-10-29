package org.unicitylabs.sdk.e2e.steps.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.AddressFactory;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.*;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.e2e.config.CucumberConfiguration;
import org.unicitylabs.sdk.e2e.context.TestContext;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicateReference;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.transaction.*;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TestUtils;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.unicitylabs.sdk.util.InclusionProofUtils.waitInclusionProof;
import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;
import static org.junit.jupiter.api.Assertions.*;

import org.unicitylabs.sdk.utils.helpers.CommitmentResult;
import org.unicitylabs.sdk.verification.VerificationResult;




/**
 * Shared step definitions that can be reused across multiple feature files.
 * These steps use TestContext to maintain state and avoid duplication.
 */
public class SharedStepDefinitions {

    private final TestContext context;

    public SharedStepDefinitions() {  // ✅ Public zero-argument constructor
        this.context = CucumberConfiguration.getTestContext();
    }

    StepHelper helper = new StepHelper();

    // Setup Steps
    @Given("the aggregator URL is configured")
    public void theAggregatorUrlIsConfigured() {
//        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        String aggregatorUrl = "http://localhost:3000";


        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");
        context.setAggregatorClient(new JsonRpcAggregatorClient(aggregatorUrl));
    }

    @And("the aggregator client is initialized")
    public void theAggregatorClientIsInitialized() {
        assertNotNull(context.getAggregatorClient(), "Aggregator client should be initialized");
    }

    @And("the state transition client is initialized")
    public void theStateTransitionClientIsInitialized() {
        context.setClient(new StateTransitionClient(context.getAggregatorClient()));
        assertNotNull(context.getClient(), "State transition client should be initialized");
    }

    @And("the following users are set up with their signing services")
    public void usersAreSetUpWithTheirSigningServices(DataTable dataTable) {
        List<String> users = dataTable.asList();
        for (String user : users) {
            TestUtils.setupUser(user, context.getUserSigningServices(), context.getUserNonces(), context.getUserSecret());
            context.getUserTokens().put(user, new ArrayList<>());
        }
    }

    // Aggregator Operations
    @When("I request the current block height")
    public void iRequestTheCurrentBlockHeight() throws Exception {
        Long blockHeight = context.getAggregatorClient().getBlockHeight().get();
        context.setBlockHeight(blockHeight);
    }

    @Then("the block height should be returned")
    public void theBlockHeightShouldBeReturned() {
        assertNotNull(context.getBlockHeight(), "Block height should not be null");
    }

    @And("the block height should be greater than {int}")
    public void theBlockHeightShouldBeGreaterThan(int minHeight) {
        assertTrue(context.getBlockHeight() > minHeight, "Block height should be greater than " + minHeight);
    }

    // Commitment Operations
    @Given("a random secret of {int} bytes")
    public void aRandomSecretOfBytes(int secretLength) {
        byte[] randomSecret = TestUtils.generateRandomBytes(secretLength);
        context.setRandomSecret(randomSecret);
        assertNotNull(randomSecret);
        assertEquals(secretLength, randomSecret.length);
    }

    @And("a state hash from {int} bytes of random data")
    public void aStateHashFromBytesOfRandomData(int stateLength) {
        byte[] stateBytes = TestUtils.generateRandomBytes(stateLength);
        DataHash stateHash = TestUtils.hashData(stateBytes);
        context.setStateBytes(stateBytes);
        context.setStateHash(stateHash);
        assertNotNull(stateHash);
    }

    @And("transaction data {string}")
    public void transactionData(String txData) {
        DataHash txDataHash = TestUtils.hashData(txData.getBytes(StandardCharsets.UTF_8));
        context.setTxDataHash(txDataHash);
        assertNotNull(txDataHash);
    }

    @When("I submit a commitment with the generated data")
    public void iSubmitACommitmentWithTheGeneratedData() throws Exception {
        long startTime = System.currentTimeMillis();

        SigningService signingService = SigningService.createFromSecret(context.getRandomSecret());
        var requestId = TestUtils.createRequestId(signingService, context.getStateHash());
        var authenticator = TestUtils.createAuthenticator(signingService, context.getTxDataHash(), context.getStateHash());

        SubmitCommitmentResponse response = context.getAggregatorClient()
                .submitCommitment(requestId, context.getTxDataHash(), authenticator).get();
        context.setCommitmentResponse(response);

        long endTime = System.currentTimeMillis();
        context.setSubmissionDuration(endTime - startTime);
    }

    @Then("the commitment should be submitted successfully")
    public void theCommitmentShouldBeSubmittedSuccessfully() {
        assertNotNull(context.getCommitmentResponse(), "Commitment response should not be null");
        assertEquals(SubmitCommitmentStatus.SUCCESS, context.getCommitmentResponse().getStatus(),
                "Commitment should be submitted successfully");
    }

    @And("the submission should complete in less than {int} milliseconds")
    public void theSubmissionShouldCompleteInLessThanMilliseconds(int maxDuration) {
        assertTrue(context.getSubmissionDuration() < maxDuration,
                String.format("Submission took %d ms, should be less than %d ms",
                        context.getSubmissionDuration(), maxDuration));
    }

    // Multi-threaded Operations
    @Given("I configure {int} threads with {int} commitments each")
    public void iConfigureThreadsWithCommitmentsEach(int threadCount, int commitmentsPerThread) {
        context.setConfiguredThreadCount(threadCount);
        context.setConfiguredCommitmentsPerThread(commitmentsPerThread);

        // Reuse existing user setup to create <threadsCount> users
        context.setConfiguredUserCount(threadCount);

        // Setup additional users if needed
        for (int i = 0; i < threadCount; i++) {
            String userName = "BulkUser" + i;
            TestUtils.setupUser(userName, context.getUserSigningServices(), context.getUserNonces(), context.getUserSecret());
            context.getUserTokens().put(userName, new ArrayList<>());
        }
    }

    @When("I submit all mint commitments concurrently")
    public void iSubmitAllMintCommitmentsConcurrently() throws Exception {
        int threadsCount = context.getConfiguredThreadCount();
        int commitmentsPerThread = context.getConfiguredCommitmentsPerThread();

        Map<String, SigningService> userSigningServices = context.getUserSigningServices();
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);

        List<CompletableFuture<CommitmentResult>> futures = new ArrayList<>();

        for (Map.Entry<String, SigningService> entry : userSigningServices.entrySet()) {
            String userName = entry.getKey();
            SigningService signingService = entry.getValue();

            for (int i = 0; i < commitmentsPerThread; i++) {
                CompletableFuture<CommitmentResult> future = CompletableFuture.supplyAsync(() -> {
                    long start = System.nanoTime();
                    byte[] stateBytes = TestUtils.generateRandomBytes(32);
                    byte[] txData = TestUtils.generateRandomBytes(32);

                    DataHash stateHash = TestUtils.hashData(stateBytes);
                    DataHash txDataHash = TestUtils.hashData(txData);
                    RequestId requestId = TestUtils.createRequestId(signingService, stateHash);

                    try {
                        Authenticator authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);

                        SubmitCommitmentResponse response = context.getAggregatorClient()
                                .submitCommitment(requestId, txDataHash, authenticator).get();

                        boolean success = response.getStatus() == SubmitCommitmentStatus.SUCCESS;
                        long end = System.nanoTime();

                        return new CommitmentResult(userName, Thread.currentThread().getName(),
                                requestId, success, start, end);
                    } catch (Exception e) {
                        long end = System.nanoTime();
                        return new CommitmentResult(userName, Thread.currentThread().getName(),
                                requestId, false, start, end);
                    }
                }, executor);

                futures.add(future);
            }
        }

        context.setCommitmentFutures(futures);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    // Token Operations
    @Given("{string} mints a token with random coin data")
    public void userMintsATokenWithRandomCoinData(String username) throws Exception {
        TokenId tokenId = TestUtils.generateRandomTokenId();
        TokenType tokenType = TestUtils.generateRandomTokenType();;
        TokenCoinData coinData = TestUtils.createRandomCoinData(2);

        // Mint with masked predicate n0 (internally)
        Token <?> token = TestUtils.mintTokenForUser(
                context.getClient(),
                context.getUserSigningServices().get(username),
                context.getUserNonces().get(username),
                tokenId,
                tokenType,
                coinData,
                context.getTrustBase()
        );

        // do post-processing here (still in parallel)
        if (TestUtils.validateTokenOwnership(
                token,
                context.getUserSigningServices().get(username),
                context.getTrustBase()
        )) {
            context.addUserToken(username, token);
        }
        context.setCurrentUser(username);
    }

    @When("{string} transfers the token to {string} using a proxy address")
    public void userTransfersTheTokenToUserUsingAProxyAddress(String fromUser, String toUser) throws Exception {
        Token sourceToken = context.getUserToken(fromUser);
        ProxyAddress proxyAddress = ProxyAddress.create(context.getNameTagToken(toUser).getId());
        helper.transferToken(fromUser, toUser, sourceToken, proxyAddress, null);
    }

    @When("{string} transfers the token to {string} using an unmasked predicate")
    public void userTransfersTheTokenToUserUsingAnUnmaskedPredicate(String fromUser, String toUser) throws Exception {
        Token sourceToken = context.getUserToken(fromUser);
        SigningService toSigningService = context.getUserSigningServices().get(toUser);

//        UnmaskedPredicate userPredicate = UnmaskedPredicate.create(
//                sourceToken.getId(),
//                sourceToken.getType(),
//                toSigningService,
//                HashAlgorithm.SHA256,
//                context.getUserNonces().get(toUser)
//        );
        SigningService carolSigningService = SigningService.createFromSecret(context.getUserSecret().get(toUser));

        UnmaskedPredicateReference reference = UnmaskedPredicateReference.create(
                sourceToken.getType(),
                carolSigningService.getAlgorithm(),
                carolSigningService.getPublicKey(),
                HashAlgorithm.SHA256
        );

        DirectAddress toAddress = reference.toAddress();

        helper.transferToken(fromUser, toUser, sourceToken, toAddress, null);
    }

    @Then("{string} should own the token successfully")
    public void userShouldOwnTheTokenSuccessfully(String username) {
        Token token = context.getUserToken(username);
        context.setCurrentUser(username);
        SigningService signingService = context.getUserSigningServices().get(username);
        VerificationResult result = token.verify(context.getTrustBase());
        assertTrue(result.isSuccessful(), () -> "Token should be valid but failed with reason: " + result);

        assertTrue(PredicateEngineService.createPredicate(token.getState().getPredicate())
                .isOwner(helper.getSigningServiceForToken(username, token).getPublicKey()), username + " should own the token");
    }

    @Then("all mint commitments should receive inclusion proofs within {int} seconds")
    public void allMintCommitmentsShouldReceiveInclusionProofs(int timeoutSeconds) throws Exception {
        List<CommitmentResult> results = helper.collectCommitmentResults();
        helper.verifyAllInclusionProofsInParallel(timeoutSeconds);

        long verifiedCount = results.stream()
                .filter(CommitmentResult::isVerified)
                .count();

        System.out.println("Verified commitments: " + verifiedCount + " / " + results.size());
        // Print failed ones (not verified)
        results.stream()
                .filter(r -> !r.isVerified())
                .forEach(r -> System.out.println(
                        "❌ Commitment failed: requestId=" + r.getRequestId().toString() + ", status=" + r.getStatus()
                ));

        assertEquals(results.size(), verifiedCount, "All commitments should be verified");
    }

    @Given("user {string} create a nametag token with custom data {string}")
    public void userCreateANametagTokenWithCustomData(String username, String customData) throws Exception {
        Token token = context.getUserToken(context.getCurrentUser());
        context.setCurrentUser(username);
        String nameTagIdentifier = TestUtils.generateRandomString(10);
        Token nametagToken = helper.createNameTagTokenForUser(
                username,
                token,
                nameTagIdentifier,
                customData
        );
        assertNotNull(nametagToken, "Name tag token should be created");
        assertTrue(nametagToken.verify(context.getTrustBase()).isSuccessful(), "Name tag token should be valid");
        context.addNameTagToken(username, nametagToken);
    }

    @Given("the aggregator URLs are configured")
    public void theAggregatorURLsAreConfigured(DataTable dataTable) {
        List<String> aggregatorUrls = dataTable.asList();

        // You can either use environment variables or hardcode the URLs
//        List<String> aggregatorUrls = Arrays.asList(
////                System.getenv("AGGREGATOR_URL")
//                "http://localhost:3001"
//        );

        assertNotNull(aggregatorUrls, "Aggregator URLs must be configured");
        assertFalse(aggregatorUrls.isEmpty(), "At least one aggregator URL must be provided");

        List<AggregatorClient> clients = new ArrayList<>();
        for (String url : aggregatorUrls) {
            clients.add(new JsonRpcAggregatorClient(url.trim(), "premium-key-abc"));
        }

        context.setAggregatorClients(clients);
    }

    @And("the aggregator clients are initialized")
    public void theAggregatorClientsAreInitialized() {
        List<AggregatorClient> clients = context.getAggregatorClients();
        assertNotNull(clients, "Aggregator clients should be initialized");
        assertFalse(clients.isEmpty(), "At least one aggregator client should be initialized");
    }

    @When("I submit all mint commitments concurrently to all aggregators")
    public void iSubmitAllMintCommitmentsConcurrentlyToAllAggregators() {
        int threadsCount = context.getConfiguredThreadCount();
        int commitmentsPerThread = context.getConfiguredCommitmentsPerThread();
        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();

        Map<String, SigningService> userSigningServices = context.getUserSigningServices();

        // Calculate total thread pool size: threads * aggregators
        int totalThreadPoolSize = threadsCount * aggregatorClients.size();
        ExecutorService executor = Executors.newFixedThreadPool(totalThreadPoolSize);

        List<CompletableFuture<CommitmentResult>> futures = new ArrayList<>();

        for (Map.Entry<String, SigningService> entry : userSigningServices.entrySet()) {
            String userName = entry.getKey();
            SigningService signingService = entry.getValue();

            for (int i = 0; i < commitmentsPerThread; i++) {
                // Generate the commitment data once for this iteration
                byte[] stateBytes = TestUtils.generateRandomBytes(32);
                byte[] txData = TestUtils.generateRandomBytes(32);
                DataHash stateHash = TestUtils.hashData(stateBytes);
                DataHash txDataHash = TestUtils.hashData(txData);
                RequestId requestId = TestUtils.createRequestId(signingService, stateHash);

                // Submit the same commitment to all aggregators concurrently
                for (int aggIndex = 0; aggIndex < aggregatorClients.size(); aggIndex++) {
                    AggregatorClient aggregatorClient = aggregatorClients.get(aggIndex);
                    String aggregatorId = "Aggregator" + aggIndex;

                    CompletableFuture<CommitmentResult> future = CompletableFuture.supplyAsync(() -> {
                        long start = System.nanoTime();

                        try {
                            Authenticator authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);

                            SubmitCommitmentResponse response = aggregatorClient
                                    .submitCommitment(requestId, txDataHash, authenticator).get();

                            boolean success = response.getStatus() == SubmitCommitmentStatus.SUCCESS;
                            long end = System.nanoTime();

                            return new CommitmentResult(userName + "-" + aggregatorId,
                                    Thread.currentThread().getName(),
                                    requestId, success, start, end);
                        } catch (Exception e) {
                            long end = System.nanoTime();
                            return new CommitmentResult(userName + "-" + aggregatorId,
                                    Thread.currentThread().getName(),
                                    requestId, false, start, end);
                        }
                    }, executor);

                    futures.add(future);
                }
            }
        }

        context.setCommitmentFutures(futures);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    @Then("all commitments should be processed successfully")
    public void allCommitmentsShouldBeProcessedSuccessfully() {
        int threadsCount = context.getConfiguredThreadCount();
        int commitmentsPerThread = context.getConfiguredCommitmentsPerThread();
        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();

        Map<String, SigningService> userSigningServices = context.getUserSigningServices();
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);

        List<CompletableFuture<List<CommitmentResult>>> futures = new ArrayList<>();

        for (Map.Entry<String, SigningService> entry : userSigningServices.entrySet()) {
            String userName = entry.getKey();
            SigningService signingService = entry.getValue();

            for (int i = 0; i < commitmentsPerThread; i++) {
                CompletableFuture<List<CommitmentResult>> future = CompletableFuture.supplyAsync(() -> {
                    List<CommitmentResult> results = new ArrayList<>();

                    // Generate commitment data once
                    byte[] stateBytes = TestUtils.generateRandomBytes(32);
                    byte[] txData = TestUtils.generateRandomBytes(32);
                    DataHash stateHash = TestUtils.hashData(stateBytes);
                    DataHash txDataHash = TestUtils.hashData(txData);
                    RequestId requestId = TestUtils.createRequestId(signingService, stateHash);

                    // Submit to all aggregators with the same data
                    for (int aggIndex = 0; aggIndex < aggregatorClients.size(); aggIndex++) {
                        AggregatorClient aggregatorClient = aggregatorClients.get(aggIndex);
                        String aggregatorId = "Aggregator" + aggIndex;

                        long start = System.nanoTime();
                        try {
                            Authenticator authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);

                            SubmitCommitmentResponse response = aggregatorClient
                                    .submitCommitment(requestId, txDataHash, authenticator).get();

                            boolean success = response.getStatus() == SubmitCommitmentStatus.SUCCESS;
                            long end = System.nanoTime();

                            results.add(new CommitmentResult(userName + "-" + aggregatorId,
                                    Thread.currentThread().getName(),
                                    requestId, success, start, end));
                        } catch (Exception e) {
                            long end = System.nanoTime();
                            results.add(new CommitmentResult(userName + "-" + aggregatorId,
                                    Thread.currentThread().getName(),
                                    requestId, false, start, end));
                        }
                    }

                    return results;
                }, executor);

                futures.add(future);
            }
        }

        // Flatten the results
        List<CompletableFuture<CommitmentResult>> flattenedFutures = new ArrayList<>();
        for (CompletableFuture<List<CommitmentResult>> future : futures) {
            CompletableFuture<CommitmentResult> flattened = future.thenCompose(results -> {
                // Return the first result (or you could return all)
                return CompletableFuture.completedFuture(results.get(0));
            });
            flattenedFutures.add(flattened);
        }

        context.setCommitmentFutures(flattenedFutures);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();
    }

    @Then("all mint commitments should receive inclusion proofs from all aggregators within {int} seconds")
    public void allMintCommitmentsShouldReceiveInclusionProofsFromAllAggregatorsWithinSeconds(int timeoutSeconds) throws Exception {
        List<CommitmentResult> results = helper.collectCommitmentResults();

        // Verify inclusion proofs for all aggregators in parallel
        helper.verifyAllInclusionProofsInParallelForMultipleAggregators(timeoutSeconds, context.getAggregatorClients());

        long verifiedCount = results.stream()
                .filter(CommitmentResult::isVerified)
                .count();

        System.out.println("=== Inclusion Proof Verification Results ===");
        System.out.println("Total commitments: " + results.size());
        System.out.println("Verified commitments: " + verifiedCount + " / " + results.size());

        // Group results by aggregator for detailed reporting
        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
                .collect(Collectors.groupingBy(r -> helper.extractAggregatorFromUserName(r.getUserName())));

        for (Map.Entry<String, List<CommitmentResult>> entry : resultsByAggregator.entrySet()) {
            String aggregatorId = entry.getKey();
            List<CommitmentResult> aggregatorResults = entry.getValue();

            long aggregatorVerifiedCount = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .count();

            System.out.println("\n" + aggregatorId + ":");
            System.out.println("  Verified: " + aggregatorVerifiedCount + " / " + aggregatorResults.size());

            // Print failed ones for this aggregator
            aggregatorResults.stream()
                    .filter(r -> !r.isVerified())
                    .forEach(r -> System.out.println(
                            "  ❌ Failed: requestId=" + r.getRequestId().toString() +
                                    ", status=" + (r.getStatus() != null ? r.getStatus() : "Unknown") +
                                    ", user=" + r.getUserName()
                    ));

            // Print successful ones (optional, for debugging)
            if (aggregatorVerifiedCount > 0) {
                System.out.println("  ✅ Successfully verified " + aggregatorVerifiedCount + " commitments");
            }
        }

        assertEquals(results.size(), verifiedCount, "All commitments should be verified");
    }

    @Then("all mint commitments should receive inclusion proofs within {int} seconds with {int}% success rate")
    public void allMintCommitmentsShouldReceiveInclusionProofsWithSuccessRate(int timeoutSeconds, int expectedSuccessRate) throws Exception {
        List<CommitmentResult> results = helper.collectCommitmentResults();

        // Verify inclusion proofs for all aggregators in parallel
        helper.verifyAllInclusionProofsInParallelForMultipleAggregators(timeoutSeconds, context.getAggregatorClients());

        long verifiedCount = results.stream()
                .filter(CommitmentResult::isVerified)
                .count();

        double actualSuccessRate = (double) verifiedCount / results.size() * 100;

        System.out.println("=== Inclusion Proof Verification Results ===");
        System.out.println("Total commitments: " + results.size());
        System.out.println("Verified commitments: " + verifiedCount + " / " + results.size());
        System.out.println("Actual success rate: " + String.format("%.2f%%", actualSuccessRate));
        System.out.println("Expected success rate: " + expectedSuccessRate + "%");

        // Detailed reporting by aggregator
        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
                .collect(Collectors.groupingBy(r -> helper.extractAggregatorFromUserName(r.getUserName())));

        for (Map.Entry<String, List<CommitmentResult>> entry : resultsByAggregator.entrySet()) {
            String aggregatorId = entry.getKey();
            List<CommitmentResult> aggregatorResults = entry.getValue();

            long aggregatorVerifiedCount = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .count();

            double aggregatorSuccessRate = (double) aggregatorVerifiedCount / aggregatorResults.size() * 100;

            System.out.println("\n" + aggregatorId + ":");
            System.out.println("  Success rate: " + String.format("%.2f%%", aggregatorSuccessRate) +
                    " (" + aggregatorVerifiedCount + " / " + aggregatorResults.size() + ")");
        }

        assertTrue(actualSuccessRate >= expectedSuccessRate,
                String.format("Expected success rate of at least %d%%, but got %.2f%%",
                        expectedSuccessRate, actualSuccessRate));
    }

    @Then("I should see performance metrics for each aggregator")
    public void iShouldSeePerformanceMetricsForEachAggregator() {
        List<CommitmentResult> results = helper.collectCommitmentResults();
        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();

        System.out.println("\n=== 📊 AGGREGATOR PERFORMANCE COMPARISON ===");

        // Print detailed breakdown
        helper.printDetailedResultsByAggregator(results, aggregatorClients.size());

        // Additional performance analysis
        helper.printPerformanceComparison(results, aggregatorClients.size());
    }

    @Then("aggregator performance should meet minimum thresholds")
    public void aggregatorPerformanceShouldMeetMinimumThresholds() {
        List<CommitmentResult> results = helper.collectCommitmentResults();
        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();

        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
                .collect(Collectors.groupingBy(r -> helper.extractAggregatorFromUserName(r.getUserName())));

        for (int i = 0; i < aggregatorClients.size(); i++) {
            String aggregatorId = "-Aggregator" + i;
            List<CommitmentResult> aggregatorResults = resultsByAggregator.getOrDefault(aggregatorId, new ArrayList<>());

            if (aggregatorResults.isEmpty()) continue;

            long verifiedCount = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .count();

            double successRate = (double) verifiedCount / aggregatorResults.size() * 100;

            // Assert minimum success rate (configurable)
            assertTrue(successRate >= 90.0,
                    String.format("Aggregator%d success rate (%.2f%%) should be at least 90%%", i, successRate));

            // Assert reasonable average inclusion time
            OptionalDouble avgInclusionTime = aggregatorResults.stream()
                    .filter(CommitmentResult::isVerified)
                    .mapToDouble(CommitmentResult::getInclusionDurationMillis)
                    .average();

            if (avgInclusionTime.isPresent()) {
                assertTrue(avgInclusionTime.getAsDouble() <= 30000, // 30 seconds max
                        String.format("Aggregator%d average inclusion time (%.2fms) should be under 30 seconds",
                                i, avgInclusionTime.getAsDouble()));
            }

            System.out.println("✅ Aggregator" + i + " meets performance thresholds");
        }
    }

    @And("trust-base.json is set")
    public void trustBaseIsSet() throws IOException {
        // Write code here that turns the phrase above into concrete actions
        context.setTrustBase(UnicityObjectMapper.JSON.readValue(
                getClass().getResourceAsStream("/trust-base.json"),
                RootTrustBase.class
            )
        );
        assertNotNull(context.getTrustBase(), "trust-base.json must be set");
    }

    @When("{string} splits her token into halves as {string}")
    public void userSplitsHerTokenIntoHalves(String username, String splitMintType) throws Exception {


        Token<?> originalToken = context.getUserToken(username);
        SigningService signingService = helper.getSigningServiceForToken(username, originalToken);

        // Extract original coin data and halve each amount
        TokenCoinData originalData = originalToken.getCoins().orElseThrow(() -> new IllegalStateException("Token has no coin data"));;
        Map<CoinId, BigInteger> originalCoins = originalData.getCoins();

        Map<CoinId, BigInteger> halfCoins1 = new HashMap<>();
        Map<CoinId, BigInteger> halfCoins2 = new HashMap<>();

        for (Map.Entry<CoinId, BigInteger> entry : originalCoins.entrySet()) {
            BigInteger value = entry.getValue();
            BigInteger half = value.divide(BigInteger.valueOf(2));
            BigInteger remainder = value.mod(BigInteger.valueOf(2));
            halfCoins1.put(entry.getKey(), half);
            halfCoins2.put(entry.getKey(), half.add(remainder)); // add remainder if odd
        }

        TokenCoinData data1 = new TokenCoinData(halfCoins1);
        TokenCoinData data2 = new TokenCoinData(halfCoins2);

        Token nametagToken = context.getNameTagToken(username);

        // Create TokenSplit using TokenSplitBuilder
        TokenSplitBuilder builder = new TokenSplitBuilder();
        System.out.println("Alice address: " + ProxyAddress.create(nametagToken.getId()));
        System.out.println("Carol address: " + ProxyAddress.create(context.getNameTagToken("Carol").getId()));


        TokenSplitBuilder.TokenSplit split = builder
                .createToken(
                        new TokenId(TestUtils.generateRandomBytes(32)),
                        originalToken.getType(),
                        null,
                        data1,
                        ProxyAddress.create(
                                nametagToken.getId()
                        ),
                        TestUtils.generateRandomBytes(32),
                        null
                )
                .createToken(
                        new TokenId(TestUtils.generateRandomBytes(32)),
                        originalToken.getType(),
                        null,
                        data2,
                        ProxyAddress.create(
                                context.getNameTagToken("Carol").getId()
                        ),
                        TestUtils.generateRandomBytes(32),
                        null
                )
                .build(originalToken);

        // Submit burn commitment
        TransferCommitment burnCommitment = split.createBurnCommitment(
                TestUtils.generateRandomBytes(32),
                signingService
        );

        SubmitCommitmentResponse burnResponse = context.getClient()
                .submitCommitment(burnCommitment).get();
        assertEquals(SubmitCommitmentStatus.SUCCESS, burnResponse.getStatus(), "Burn failed");

        // Mint new split tokens
        List<MintCommitment<SplitMintReason>> mintCommitments = split.createSplitMintCommitments(
                context.getTrustBase(),
                burnCommitment.toTransaction(
                        InclusionProofUtils.waitInclusionProof(
                                context.getClient(),
                                context.getTrustBase(),
                                burnCommitment
                        ).get()
                )
        );

        List<Token> splitTokens = new ArrayList<>();
//        for (MintCommitment<SplitMintReason> commitment : mintCommitments) {
//            SubmitCommitmentResponse response = context.getClient().submitCommitment(commitment).get();
//            assertEquals(SubmitCommitmentStatus.SUCCESS, response.getStatus(), "Split mint failed");
//
//            Predicate predicate;
//            if (splitMintType == "Masked") {
//                predicate = MaskedPredicate.create(
//                        commitment.getTransactionData().getTokenId(),
//                        commitment.getTransactionData().getTokenType(),
//                        SigningService.createFromMaskedSecret(
//                                context.getUserSecret().get(username),
//                                TestUtils.generateRandomBytes(32)
//                        ),
//                        HashAlgorithm.SHA256,
//                        commitment.getTransactionData().getSalt()
//                );
//            } else {
//                predicate = UnmaskedPredicate.create(
//                        commitment.getTransactionData().getTokenId(),
//                        commitment.getTransactionData().getTokenType(),
//                        SigningService.createFromSecret(context.getUserSecret().get(username)),
//                        HashAlgorithm.SHA256,
//                        commitment.getTransactionData().getSalt()
//                );
//            }
//
//            TokenState state = new TokenState(predicate, null);
//            Token<SplitMintReason> splitToken = Token.create(
//                    context.getTrustBase(),
//                    state,
//                    commitment.toTransaction(
//                            InclusionProofUtils.waitInclusionProof(
//                                    context.getClient(),
//                                    context.getTrustBase(),
//                                    commitment
//                            ).get()
//                    ),
//                    List.of(nametagToken)
//            );
//
//            assertTrue(splitToken.verify(context.getTrustBase()).isSuccessful(), "Split token invalid");
//
//            splitTokens.add(splitToken);
//        }

        SubmitCommitmentResponse response = context.getClient().submitCommitment(mintCommitments.get(0)).get();
        assertEquals(SubmitCommitmentStatus.SUCCESS, response.getStatus(), "Split mint failed");

        Predicate predicate = UnmaskedPredicate.create(
                    mintCommitments.get(0).getTransactionData().getTokenId(),
                    mintCommitments.get(0).getTransactionData().getTokenType(),
                    SigningService.createFromSecret(context.getUserSecret().get(username)),
                    HashAlgorithm.SHA256,
                    mintCommitments.get(0).getTransactionData().getSalt()
            );

        TokenState state = new TokenState(predicate, null);
        System.out.println(mintCommitments.get(0).getTransactionData().getRecipient());
        Token<SplitMintReason> splitToken = Token.create(
                context.getTrustBase(),
                state,
                mintCommitments.get(0).toTransaction(
                        InclusionProofUtils.waitInclusionProof(
                                context.getClient(),
                                context.getTrustBase(),
                                mintCommitments.get(0)
                        ).get()
                ),
                List.of(nametagToken)
        );

        assertTrue(splitToken.verify(context.getTrustBase()).isSuccessful(), "Split token invalid");

        splitTokens.add(splitToken);

        SubmitCommitmentResponse response2 = context.getClient().submitCommitment(mintCommitments.get(1)).get();
        assertEquals(SubmitCommitmentStatus.SUCCESS, response2.getStatus(), "Split mint failed");

        Predicate predicate2 = UnmaskedPredicate.create(
                mintCommitments.get(1).getTransactionData().getTokenId(),
                mintCommitments.get(1).getTransactionData().getTokenType(),
                SigningService.createFromSecret(context.getUserSecret().get("Carol")),
                HashAlgorithm.SHA256,
                mintCommitments.get(1).getTransactionData().getSalt()
        );

        TokenState state2 = new TokenState(predicate2, null);
        Token<SplitMintReason> splitToken2 = Token.create(
                context.getTrustBase(),
                state2,
                mintCommitments.get(1).toTransaction(
                        InclusionProofUtils.waitInclusionProof(
                                context.getClient(),
                                context.getTrustBase(),
                                mintCommitments.get(1)
                        ).get()
                ),
                List.of(context.getNameTagToken("Carol"))
        );

        assertTrue(splitToken2.verify(context.getTrustBase()).isSuccessful(), "Split token 2 invalid");

        splitTokens.add(splitToken2);



        assertTrue(PredicateEngineService.createPredicate(splitTokens.get(0).getState().getPredicate())
                .isOwner(helper.getSigningServiceForToken(username, splitTokens.get(0)).getPublicKey()), username + " should own the token");
        assertTrue(PredicateEngineService.createPredicate(splitTokens.get(1).getState().getPredicate())
                .isOwner(helper.getSigningServiceForToken("Carol", splitTokens.get(1)).getPublicKey()), "Carol" + " should own the token");

        context.removeUserToken(username,originalToken);
        // Save split tokens in context
        context.getUserTokens().put(username, splitTokens);
        context.setCurrentUser(username);
    }


//    @When("{string} splits her token into {int} equal parts")
//    public void userSplitsTokenIntoNParts(String username, int parts) throws Exception {
//        Token<?> originalToken = context.getUserToken(username);
//        SigningService signingService = context.getUserSigningServices().get(username);
//
//        TokenCoinData originalData = originalToken.getCoins()
//                .orElseThrow(() -> new IllegalStateException("Token has no coin data"));
//
//        Map<CoinId, BigInteger> originalCoins = originalData.getCoins();
//
//        // Create divided coin data for each part
//        List<TokenCoinData> dividedCoinDataList = new ArrayList<>();
//
//        for (int i = 0; i < parts; i++) {
//            Map<CoinId, BigInteger> coinMap = new HashMap<>();
//            for (Map.Entry<CoinId, BigInteger> entry : originalCoins.entrySet()) {
//                BigInteger value = entry.getValue();
//                BigInteger baseSplit = value.divide(BigInteger.valueOf(parts));
//                BigInteger remainder = value.mod(BigInteger.valueOf(parts));
//                BigInteger adjusted = (i == parts - 1) ? baseSplit.add(remainder) : baseSplit;
//                coinMap.put(entry.getKey(), adjusted);
//            }
//            dividedCoinDataList.add(new TokenCoinData(coinMap));
//        }
//
//        // Build token split using nametag predicate for each part
//        TokenSplitBuilder builder = new TokenSplitBuilder();
//        for (int i = 0; i < parts; i++) {
//            TokenId newId = new TokenId(TestUtils.generateRandomBytes(32));
//            TokenCoinData coinData = dividedCoinDataList.get(i);
//            ProxyAddress proxyAddress = ProxyAddress.create(
//                    context.getNameTagToken(username).getId() // mint to user's nametag
//            );
//            builder.createToken(
//                    newId,
//                    originalToken.getState().getTokenType(),
//                    null,
//                    coinData,
//                    proxyAddress,
//                    TestUtils.generateRandomBytes(32),
//                    null
//            );
//        }
//
//        TokenSplit split = builder.build(originalToken);
//
//        // Burn the original token
//        TransferCommitment burnCommitment = split.createBurnCommitment(
//                TestUtils.generateRandomBytes(32),
//                SigningService.createFromMaskedSecret(
//                        signingService.getSecret(),
//                        ((MaskedPredicate) originalToken.getState().getPredicate()).getNonce()
//                )
//        );
//
//        SubmitCommitmentResponse burnResponse = context.getClient()
//                .submitCommitment(burnCommitment).get();
//        assertEquals(SubmitCommitmentStatus.SUCCESS, burnResponse.getStatus(), "Burn failed");
//
//        // Mint the split tokens
//        List<MintCommitment<SplitMintReason>> mintCommitments = split.createSplitMintCommitments(
//                context.getTrustBase(),
//                burnCommitment.toTransaction(
//                        InclusionProofUtils.waitInclusionProof(
//                                context.getClient(),
//                                context.getTrustBase(),
//                                burnCommitment
//                        ).get()
//                )
//        );
//
//        List<Token<?>> splitTokens = new ArrayList<>();
//        for (MintCommitment<SplitMintReason> commitment : mintCommitments) {
//            SubmitCommitmentResponse response = context.getClient().submitCommitment(commitment).get();
//            assertEquals(SubmitCommitmentStatus.SUCCESS, response.getStatus(), "Split mint failed");
//
//            TokenState state = new TokenState(
//                    UnmaskedPredicate.create(
//                            commitment.getTransactionData().getTokenId(),
//                            commitment.getTransactionData().getTokenType(),
//                            signingService,
//                            HashAlgorithm.SHA256,
//                            commitment.getTransactionData().getSalt()
//                    ),
//                    null
//            );
//
//            Token<SplitMintReason> splitToken = Token.create(
//                    context.getTrustBase(),
//                    state,
//                    commitment.toTransaction(
//                            InclusionProofUtils.waitInclusionProof(
//                                    context.getClient(),
//                                    context.getTrustBase(),
//                                    commitment
//                            ).get()
//                    ),
//                    List.of()
//            );
//
//            assertTrue(splitToken.verify(context.getTrustBase()).isSuccessful(), "Split token invalid");
//            splitTokens.add(splitToken);
//        }
//
//        context.getUserTokens().put(username, splitTokens);
//        context.setCurrentUser(username);
//
//        System.out.printf("%s successfully split token into %d parts%n", username, parts);
//    }

    @Given("each user have nametags prepared")
    public void eachUserHaveNametagsPrepared() throws Exception {
        for (Map.Entry<String, SigningService> entry : context.getUserSigningServices().entrySet()) {
            String userName = entry.getKey();

            // Create a nametag for each user
            String nameTagIdentifier = TestUtils.generateRandomString(10);
            Token nametagToken = helper.createNameTagTokenForUser(
                    userName,
                    context.getUserToken(context.getCurrentUser()),
                    nameTagIdentifier,
                    userName
            );

            assertNotNull(nametagToken, "Nametag token should be created for " + userName);
            assertTrue(nametagToken.verify(context.getTrustBase()).isSuccessful(),
                    "Nametag token should be valid for " + userName);

            context.addNameTagToken(userName, nametagToken);
        }
    }

    @And("{string} transfers one split token to {string}")
    public void userTransfersOneSplitTokenToAnother(String fromUser, String toUser) throws Exception {
        List<Token> tokens = context.getUserTokens().get(fromUser);
        assertNotNull(tokens, "No split tokens found for " + fromUser);
        assertFalse(tokens.isEmpty(), "No split tokens available");

        Token<?> tokenToTransfer = tokens.get(0); // send one half
        ProxyAddress proxyAddress = ProxyAddress.create(
                context.getNameTagToken(toUser).getId()
        );

        helper.transferToken(fromUser, toUser, tokenToTransfer, proxyAddress, null);
    }

    @When("{string} transfers the token to direct address {string}")
    public void transfersTheTokenToDirectAddress(String fromUser, String directAddress) throws Exception {
        Token sourceToken = context.getUserToken(fromUser);
        String sourceJsonToken = sourceToken.toJson();
        System.out.println(sourceJsonToken);

        ObjectMapper mapper = new ObjectMapper();

        byte[] salt = new byte[32]; // 256-bit salt
        new SecureRandom().nextBytes(salt);

        String sessionId = "09b3ed23-6ffd-48bc-a48e-d3c3a59b3e2e";
        String saltString = Base64.getEncoder().encodeToString(salt);


        // Create data hash and state data if custom data provided
        DataHash dataHash = null;

        // Submit transfer commitment
        TransferCommitment transferCommitment = TransferCommitment.create(
                sourceToken,
                AddressFactory.createAddress(directAddress),
                randomBytes(32),
                dataHash,
                null,
                context.getUserSigningServices().get(fromUser)
        );

        SubmitCommitmentResponse response = context.getClient().submitCommitment(transferCommitment).get();
        if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
            throw new Exception("Failed to submit transfer commitment: " + response.getStatus());
        }

        // Wait for inclusion proof
        InclusionProof inclusionProof = waitInclusionProof(
                context.getClient(),
                context.getTrustBase(),
                transferCommitment
        ).get();
        TransferTransaction transferTransaction = transferCommitment.toTransaction(
                inclusionProof
        );
        System.out.println(transferTransaction.toJson());
        Map<String, Object> payload = Map.of(
                "sessionId", sessionId,
                "salt", saltString,
                // Escape the inner JSONs so they become valid JSON string values
                "transferCommitmentJson", UnicityObjectMapper.JSON.writeValueAsString(transferCommitment),
                "sourceTokenJson", UnicityObjectMapper.JSON.writeValueAsString(sourceToken)
        );

        System.out.println(UnicityObjectMapper.JSON.writeValueAsString(payload));

       mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(payload);

        System.out.println(prettyJson);
    }
}