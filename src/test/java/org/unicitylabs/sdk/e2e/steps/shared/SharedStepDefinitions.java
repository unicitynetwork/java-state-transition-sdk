//package org.unicitylabs.sdk.e2e.steps.shared;
//
//import org.unicitylabs.sdk.StateTransitionClient;
//import org.unicitylabs.sdk.address.DirectAddress;
//import org.unicitylabs.sdk.address.ProxyAddress;
//import org.unicitylabs.sdk.api.*;
//import org.unicitylabs.sdk.e2e.config.CucumberConfiguration;
//import org.unicitylabs.sdk.e2e.context.TestContext;
//import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
//import org.unicitylabs.sdk.transaction.*;
//import org.unicitylabs.sdk.utils.TestUtils;
//import org.unicitylabs.sdk.hash.DataHash;
//import org.unicitylabs.sdk.hash.HashAlgorithm;
//import org.unicitylabs.sdk.signing.SigningService;
//import org.unicitylabs.sdk.token.Token;
//import org.unicitylabs.sdk.token.TokenId;
//import org.unicitylabs.sdk.token.TokenType;
//import org.unicitylabs.sdk.token.fungible.TokenCoinData;
//import io.cucumber.datatable.DataTable;
//import io.cucumber.java.en.And;
//import io.cucumber.java.en.Given;
//import io.cucumber.java.en.Then;
//import io.cucumber.java.en.When;
//
//import java.nio.charset.StandardCharsets;
//import java.util.*;
//import java.util.concurrent.*;
//import java.util.stream.Collectors;
//
//import static org.unicitylabs.sdk.utils.TestUtils.randomCoinData;
//import static org.junit.jupiter.api.Assertions.*;
//
//import org.unicitylabs.sdk.utils.helpers.CommitmentResult;
//
//
///**
// * Shared step definitions that can be reused across multiple feature files.
// * These steps use TestContext to maintain state and avoid duplication.
// */
//public class SharedStepDefinitions {
//
//    private final TestContext context;
//
//    public SharedStepDefinitions() {  // ✅ Public zero-argument constructor
//        this.context = CucumberConfiguration.getTestContext();
//    }
//
//    StepHelper helper = new StepHelper();
//
//    // Setup Steps
//    @Given("the aggregator URL is configured")
//    public void theAggregatorUrlIsConfigured() {
//        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
//        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");
//        context.setAggregatorClient(new AggregatorClient(aggregatorUrl));
//    }
//
//    @And("the aggregator client is initialized")
//    public void theAggregatorClientIsInitialized() {
//        assertNotNull(context.getAggregatorClient(), "Aggregator client should be initialized");
//    }
//
//    @And("the state transition client is initialized")
//    public void theStateTransitionClientIsInitialized() {
//        context.setClient(new StateTransitionClient(context.getAggregatorClient()));
//        assertNotNull(context.getClient(), "State transition client should be initialized");
//    }
//
//    @And("the following users are set up with their signing services")
//    public void usersAreSetUpWithTheirSigningServices(DataTable dataTable) {
//        List<String> users = dataTable.asList();
//        for (String user : users) {
//            TestUtils.setupUser(user, context.getUserSigningServices(), context.getUserNonces(), context.getUserSecret());
//            context.getUserTokens().put(user, new ArrayList<>());
//        }
//    }
//
//    // Aggregator Operations
//    @When("I request the current block height")
//    public void iRequestTheCurrentBlockHeight() throws Exception {
//        Long blockHeight = context.getAggregatorClient().getBlockHeight().get();
//        context.setBlockHeight(blockHeight);
//    }
//
//    @Then("the block height should be returned")
//    public void theBlockHeightShouldBeReturned() {
//        assertNotNull(context.getBlockHeight(), "Block height should not be null");
//    }
//
//    @And("the block height should be greater than {int}")
//    public void theBlockHeightShouldBeGreaterThan(int minHeight) {
//        assertTrue(context.getBlockHeight() > minHeight, "Block height should be greater than " + minHeight);
//    }
//
//    // Commitment Operations
//    @Given("a random secret of {int} bytes")
//    public void aRandomSecretOfBytes(int secretLength) {
//        byte[] randomSecret = TestUtils.generateRandomBytes(secretLength);
//        context.setRandomSecret(randomSecret);
//        assertNotNull(randomSecret);
//        assertEquals(secretLength, randomSecret.length);
//    }
//
//    @And("a state hash from {int} bytes of random data")
//    public void aStateHashFromBytesOfRandomData(int stateLength) {
//        byte[] stateBytes = TestUtils.generateRandomBytes(stateLength);
//        DataHash stateHash = TestUtils.hashData(stateBytes);
//        context.setStateBytes(stateBytes);
//        context.setStateHash(stateHash);
//        assertNotNull(stateHash);
//    }
//
//    @And("transaction data {string}")
//    public void transactionData(String txData) {
//        DataHash txDataHash = TestUtils.hashData(txData.getBytes(StandardCharsets.UTF_8));
//        context.setTxDataHash(txDataHash);
//        assertNotNull(txDataHash);
//    }
//
//    @When("I submit a commitment with the generated data")
//    public void iSubmitACommitmentWithTheGeneratedData() throws Exception {
//        long startTime = System.currentTimeMillis();
//
//        SigningService signingService = SigningService.createFromSecret(context.getRandomSecret());
//        var requestId = TestUtils.createRequestId(signingService, context.getStateHash());
//        var authenticator = TestUtils.createAuthenticator(signingService, context.getTxDataHash(), context.getStateHash());
//
//        SubmitCommitmentResponse response = context.getAggregatorClient()
//                .submitCommitment(requestId, context.getTxDataHash(), authenticator).get();
//        context.setCommitmentResponse(response);
//
//        long endTime = System.currentTimeMillis();
//        context.setSubmissionDuration(endTime - startTime);
//    }
//
//    @Then("the commitment should be submitted successfully")
//    public void theCommitmentShouldBeSubmittedSuccessfully() {
//        assertNotNull(context.getCommitmentResponse(), "Commitment response should not be null");
//        assertEquals(SubmitCommitmentStatus.SUCCESS, context.getCommitmentResponse().getStatus(),
//                "Commitment should be submitted successfully");
//    }
//
//    @And("the submission should complete in less than {int} milliseconds")
//    public void theSubmissionShouldCompleteInLessThanMilliseconds(int maxDuration) {
//        assertTrue(context.getSubmissionDuration() < maxDuration,
//                String.format("Submission took %d ms, should be less than %d ms",
//                        context.getSubmissionDuration(), maxDuration));
//    }
//
//    // Multi-threaded Operations
//    @Given("I configure {int} threads with {int} commitments each")
//    public void iConfigureThreadsWithCommitmentsEach(int threadCount, int commitmentsPerThread) {
//        context.setConfiguredThreadCount(threadCount);
//        context.setConfiguredCommitmentsPerThread(commitmentsPerThread);
//
//        // Reuse existing user setup to create <threadsCount> users
//        context.setConfiguredUserCount(threadCount);
//
//        // Setup additional users if needed
//        for (int i = 0; i < threadCount; i++) {
//            String userName = "BulkUser" + i;
//            TestUtils.setupUser(userName, context.getUserSigningServices(), context.getUserNonces(), context.getUserSecret());
//            context.getUserTokens().put(userName, new ArrayList<>());
//        }
//    }
//
//    @When("I submit all mint commitments concurrently")
//    public void iSubmitAllMintCommitmentsConcurrently() throws Exception {
//        int threadsCount = context.getConfiguredThreadCount();
//        int commitmentsPerThread = context.getConfiguredCommitmentsPerThread();
//
//        Map<String, SigningService> userSigningServices = context.getUserSigningServices();
//        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
//
//        List<CompletableFuture<CommitmentResult>> futures = new ArrayList<>();
//
//        for (Map.Entry<String, SigningService> entry : userSigningServices.entrySet()) {
//            String userName = entry.getKey();
//            SigningService signingService = entry.getValue();
//
//            for (int i = 0; i < commitmentsPerThread; i++) {
//                CompletableFuture<CommitmentResult> future = CompletableFuture.supplyAsync(() -> {
//                    long start = System.nanoTime();
//                    byte[] stateBytes = TestUtils.generateRandomBytes(32);
//                    byte[] txData = TestUtils.generateRandomBytes(32);
//
//                    DataHash stateHash = TestUtils.hashData(stateBytes);
//                    DataHash txDataHash = TestUtils.hashData(txData);
//                    RequestId requestId = TestUtils.createRequestId(signingService, stateHash);
//
//                    try {
//                        Authenticator authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);
//
//                        SubmitCommitmentResponse response = context.getAggregatorClient()
//                                .submitCommitment(requestId, txDataHash, authenticator).get();
//
//                        boolean success = response.getStatus() == SubmitCommitmentStatus.SUCCESS;
//                        long end = System.nanoTime();
//
//                        return new CommitmentResult(userName, Thread.currentThread().getName(),
//                                requestId, success, start, end);
//                    } catch (Exception e) {
//                        long end = System.nanoTime();
//                        return new CommitmentResult(userName, Thread.currentThread().getName(),
//                                requestId, false, start, end);
//                    }
//                }, executor);
//
//                futures.add(future);
//            }
//        }
//
//        context.setCommitmentFutures(futures);
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//        executor.shutdown();
//    }
//
//    // Token Operations
//    @Given("{string} mints a token with random coin data")
//    public void userMintsATokenWithRandomCoinData(String username) throws Exception {
//        TokenId tokenId = TestUtils.generateRandomTokenId();
//        TokenType tokenType = context.getNameTagToken(context.getCurrentUser()).getType();
//        TokenCoinData coinData = randomCoinData(2);
//
//        Token token = TestUtils.mintTokenForUser(
//                context.getClient(),
//                context.getUserSigningServices().get(username),
//                context.getUserNonces().get(username),
//                tokenId,
//                tokenType,
//                coinData
//        );
//
//        context.addUserToken(username, token);
//        context.setCurrentUser(username);
//    }
//
//    @When("{string} transfers the token to {string} using a proxy address")
//    public void userTransfersTheTokenToUserUsingAProxyAddress(String fromUser, String toUser) throws Exception {
//        Token sourceToken = context.getUserToken(fromUser);
//        ProxyAddress proxyAddress = ProxyAddress.create(context.getNameTagToken(toUser).getId());
//        helper.transferToken(fromUser, toUser, sourceToken, proxyAddress, null);
//    }
//
//    @When("{string} transfers the token to {string} using an unmasked predicate")
//    public void userTransfersTheTokenToUserUsingAnUnmaskedPredicate(String fromUser, String toUser) throws Exception {
//        Token sourceToken = context.getUserToken(fromUser);
//        SigningService toSigningService = context.getUserSigningServices().get(toUser);
//
//        UnmaskedPredicate userPredicate = UnmaskedPredicate.create(
//                toSigningService,
//                HashAlgorithm.SHA256,
//                context.getUserNonces().get(toUser)
//        );
//        context.getUserPredicate().put(toUser, userPredicate);
//
//        DirectAddress toAddress = userPredicate.getReference(sourceToken.getType()).toAddress();
//
//        helper.transferToken(fromUser, toUser, sourceToken, toAddress, null);
//    }
//
//    @Then("{string} should own the token successfully")
//    public void userShouldOwnTheTokenSuccessfully(String username) {
//        Token token = context.getUserToken(username);
//        context.setCurrentUser(username);
//        SigningService signingService = context.getUserSigningServices().get(username);
//        assertTrue(token.verify().isSuccessful(), "Token should be valid");
//        assertTrue(token.getState().getUnlockPredicate().isOwner(signingService.getPublicKey()),
//               username + " should own the token");
//    }
//
//    @Then("all mint commitments should receive inclusion proofs within {int} seconds")
//    public void allMintCommitmentsShouldReceiveInclusionProofs(int timeoutSeconds) throws Exception {
//        List<CommitmentResult> results = helper.collectCommitmentResults();
//        helper.verifyAllInclusionProofsInParallel(timeoutSeconds);
//
//        long verifiedCount = results.stream()
//                .filter(CommitmentResult::isVerified)
//                .count();
//
//        System.out.println("Verified commitments: " + verifiedCount + " / " + results.size());
//        // Print failed ones (not verified)
//        results.stream()
//                .filter(r -> !r.isVerified())
//                .forEach(r -> System.out.println(
//                        "❌ Commitment failed: requestId=" + r.getRequestId().toString() + ", status=" + r.getStatus()
//                ));
//
//        assertEquals(results.size(), verifiedCount, "All commitments should be verified");
//    }
//
//    @Given("user {string} create a nametag token with custom data {string}")
//    public void userCreateANametagTokenWithCustomData(String username, String customData) throws Exception {
//        Token nametagToken = helper.createNameTagTokenForUser(
//                username,
//                TestUtils.generateRandomTokenType(),
//                java.util.UUID.randomUUID().toString(),
//                customData
//        );
//        assertNotNull(nametagToken, "Name tag token should be created");
//        assertTrue(nametagToken.verify().isSuccessful(), "Name tag token should be valid");
//        context.addNameTagToken(username, nametagToken);
//        context.setCurrentUser(username);
//    }
//
//    @Given("the aggregator URLs are configured")
//    public void theAggregatorURLsAreConfigured() {
//        // You can either use environment variables or hardcode the URLs
//        List<String> aggregatorUrls = Arrays.asList(
//                System.getenv("AGGREGATOR_URL")
//        );
//
//        assertNotNull(aggregatorUrls, "Aggregator URLs must be configured");
//        assertFalse(aggregatorUrls.isEmpty(), "At least one aggregator URL must be provided");
//
//        List<AggregatorClient> clients = new ArrayList<>();
//        for (String url : aggregatorUrls) {
//            clients.add(new AggregatorClient(url.trim()));
//        }
//
//        context.setAggregatorClients(clients);
//    }
//
//    @And("the aggregator clients are initialized")
//    public void theAggregatorClientsAreInitialized() {
//        List<AggregatorClient> clients = context.getAggregatorClients();
//        assertNotNull(clients, "Aggregator clients should be initialized");
//        assertFalse(clients.isEmpty(), "At least one aggregator client should be initialized");
//    }
//
//    @When("I submit all mint commitments concurrently to all aggregators")
//    public void iSubmitAllMintCommitmentsConcurrentlyToAllAggregators() {
//        int threadsCount = context.getConfiguredThreadCount();
//        int commitmentsPerThread = context.getConfiguredCommitmentsPerThread();
//        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();
//
//        Map<String, SigningService> userSigningServices = context.getUserSigningServices();
//
//        // Calculate total thread pool size: threads * aggregators
//        int totalThreadPoolSize = threadsCount * aggregatorClients.size();
//        ExecutorService executor = Executors.newFixedThreadPool(totalThreadPoolSize);
//
//        List<CompletableFuture<CommitmentResult>> futures = new ArrayList<>();
//
//        for (Map.Entry<String, SigningService> entry : userSigningServices.entrySet()) {
//            String userName = entry.getKey();
//            SigningService signingService = entry.getValue();
//
//            for (int i = 0; i < commitmentsPerThread; i++) {
//                // Generate the commitment data once for this iteration
//                byte[] stateBytes = TestUtils.generateRandomBytes(32);
//                byte[] txData = TestUtils.generateRandomBytes(32);
//                DataHash stateHash = TestUtils.hashData(stateBytes);
//                DataHash txDataHash = TestUtils.hashData(txData);
//                RequestId requestId = TestUtils.createRequestId(signingService, stateHash);
//
//                // Submit the same commitment to all aggregators concurrently
//                for (int aggIndex = 0; aggIndex < aggregatorClients.size(); aggIndex++) {
//                    AggregatorClient aggregatorClient = aggregatorClients.get(aggIndex);
//                    String aggregatorId = "Aggregator" + aggIndex;
//
//                    CompletableFuture<CommitmentResult> future = CompletableFuture.supplyAsync(() -> {
//                        long start = System.nanoTime();
//
//                        try {
//                            Authenticator authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);
//
//                            SubmitCommitmentResponse response = aggregatorClient
//                                    .submitCommitment(requestId, txDataHash, authenticator).get();
//
//                            boolean success = response.getStatus() == SubmitCommitmentStatus.SUCCESS;
//                            long end = System.nanoTime();
//
//                            return new CommitmentResult(userName + "-" + aggregatorId,
//                                    Thread.currentThread().getName(),
//                                    requestId, success, start, end);
//                        } catch (Exception e) {
//                            long end = System.nanoTime();
//                            return new CommitmentResult(userName + "-" + aggregatorId,
//                                    Thread.currentThread().getName(),
//                                    requestId, false, start, end);
//                        }
//                    }, executor);
//
//                    futures.add(future);
//                }
//            }
//        }
//
//        context.setCommitmentFutures(futures);
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//        executor.shutdown();
//    }
//
//    @Then("all commitments should be processed successfully")
//    public void allCommitmentsShouldBeProcessedSuccessfully() {
//        int threadsCount = context.getConfiguredThreadCount();
//        int commitmentsPerThread = context.getConfiguredCommitmentsPerThread();
//        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();
//
//        Map<String, SigningService> userSigningServices = context.getUserSigningServices();
//        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
//
//        List<CompletableFuture<List<CommitmentResult>>> futures = new ArrayList<>();
//
//        for (Map.Entry<String, SigningService> entry : userSigningServices.entrySet()) {
//            String userName = entry.getKey();
//            SigningService signingService = entry.getValue();
//
//            for (int i = 0; i < commitmentsPerThread; i++) {
//                CompletableFuture<List<CommitmentResult>> future = CompletableFuture.supplyAsync(() -> {
//                    List<CommitmentResult> results = new ArrayList<>();
//
//                    // Generate commitment data once
//                    byte[] stateBytes = TestUtils.generateRandomBytes(32);
//                    byte[] txData = TestUtils.generateRandomBytes(32);
//                    DataHash stateHash = TestUtils.hashData(stateBytes);
//                    DataHash txDataHash = TestUtils.hashData(txData);
//                    RequestId requestId = TestUtils.createRequestId(signingService, stateHash);
//
//                    // Submit to all aggregators with the same data
//                    for (int aggIndex = 0; aggIndex < aggregatorClients.size(); aggIndex++) {
//                        AggregatorClient aggregatorClient = aggregatorClients.get(aggIndex);
//                        String aggregatorId = "Aggregator" + aggIndex;
//
//                        long start = System.nanoTime();
//                        try {
//                            Authenticator authenticator = TestUtils.createAuthenticator(signingService, txDataHash, stateHash);
//
//                            SubmitCommitmentResponse response = aggregatorClient
//                                    .submitCommitment(requestId, txDataHash, authenticator).get();
//
//                            boolean success = response.getStatus() == SubmitCommitmentStatus.SUCCESS;
//                            long end = System.nanoTime();
//
//                            results.add(new CommitmentResult(userName + "-" + aggregatorId,
//                                    Thread.currentThread().getName(),
//                                    requestId, success, start, end));
//                        } catch (Exception e) {
//                            long end = System.nanoTime();
//                            results.add(new CommitmentResult(userName + "-" + aggregatorId,
//                                    Thread.currentThread().getName(),
//                                    requestId, false, start, end));
//                        }
//                    }
//
//                    return results;
//                }, executor);
//
//                futures.add(future);
//            }
//        }
//
//        // Flatten the results
//        List<CompletableFuture<CommitmentResult>> flattenedFutures = new ArrayList<>();
//        for (CompletableFuture<List<CommitmentResult>> future : futures) {
//            CompletableFuture<CommitmentResult> flattened = future.thenCompose(results -> {
//                // Return the first result (or you could return all)
//                return CompletableFuture.completedFuture(results.get(0));
//            });
//            flattenedFutures.add(flattened);
//        }
//
//        context.setCommitmentFutures(flattenedFutures);
//
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//        executor.shutdown();
//    }
//
//    @Then("all mint commitments should receive inclusion proofs from all aggregators within {int} seconds")
//    public void allMintCommitmentsShouldReceiveInclusionProofsFromAllAggregatorsWithinSeconds(int timeoutSeconds) throws Exception {
//        List<CommitmentResult> results = helper.collectCommitmentResults();
//
//        // Verify inclusion proofs for all aggregators in parallel
//        helper.verifyAllInclusionProofsInParallelForMultipleAggregators(timeoutSeconds, context.getAggregatorClients());
//
//        long verifiedCount = results.stream()
//                .filter(CommitmentResult::isVerified)
//                .count();
//
//        System.out.println("=== Inclusion Proof Verification Results ===");
//        System.out.println("Total commitments: " + results.size());
//        System.out.println("Verified commitments: " + verifiedCount + " / " + results.size());
//
//        // Group results by aggregator for detailed reporting
//        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
//                .collect(Collectors.groupingBy(r -> helper.extractAggregatorFromUserName(r.getUserName())));
//
//        for (Map.Entry<String, List<CommitmentResult>> entry : resultsByAggregator.entrySet()) {
//            String aggregatorId = entry.getKey();
//            List<CommitmentResult> aggregatorResults = entry.getValue();
//
//            long aggregatorVerifiedCount = aggregatorResults.stream()
//                    .filter(CommitmentResult::isVerified)
//                    .count();
//
//            System.out.println("\n" + aggregatorId + ":");
//            System.out.println("  Verified: " + aggregatorVerifiedCount + " / " + aggregatorResults.size());
//
//            // Print failed ones for this aggregator
//            aggregatorResults.stream()
//                    .filter(r -> !r.isVerified())
//                    .forEach(r -> System.out.println(
//                            "  ❌ Failed: requestId=" + r.getRequestId().toString() +
//                                    ", status=" + (r.getStatus() != null ? r.getStatus() : "Unknown") +
//                                    ", user=" + r.getUserName()
//                    ));
//
//            // Print successful ones (optional, for debugging)
//            if (aggregatorVerifiedCount > 0) {
//                System.out.println("  ✅ Successfully verified " + aggregatorVerifiedCount + " commitments");
//            }
//        }
//
//        assertEquals(results.size(), verifiedCount, "All commitments should be verified");
//    }
//
//    @Then("all mint commitments should receive inclusion proofs within {int} seconds with {int}% success rate")
//    public void allMintCommitmentsShouldReceiveInclusionProofsWithSuccessRate(int timeoutSeconds, int expectedSuccessRate) throws Exception {
//        List<CommitmentResult> results = helper.collectCommitmentResults();
//
//        // Verify inclusion proofs for all aggregators in parallel
//        helper.verifyAllInclusionProofsInParallelForMultipleAggregators(timeoutSeconds, context.getAggregatorClients());
//
//        long verifiedCount = results.stream()
//                .filter(CommitmentResult::isVerified)
//                .count();
//
//        double actualSuccessRate = (double) verifiedCount / results.size() * 100;
//
//        System.out.println("=== Inclusion Proof Verification Results ===");
//        System.out.println("Total commitments: " + results.size());
//        System.out.println("Verified commitments: " + verifiedCount + " / " + results.size());
//        System.out.println("Actual success rate: " + String.format("%.2f%%", actualSuccessRate));
//        System.out.println("Expected success rate: " + expectedSuccessRate + "%");
//
//        // Detailed reporting by aggregator
//        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
//                .collect(Collectors.groupingBy(r -> helper.extractAggregatorFromUserName(r.getUserName())));
//
//        for (Map.Entry<String, List<CommitmentResult>> entry : resultsByAggregator.entrySet()) {
//            String aggregatorId = entry.getKey();
//            List<CommitmentResult> aggregatorResults = entry.getValue();
//
//            long aggregatorVerifiedCount = aggregatorResults.stream()
//                    .filter(CommitmentResult::isVerified)
//                    .count();
//
//            double aggregatorSuccessRate = (double) aggregatorVerifiedCount / aggregatorResults.size() * 100;
//
//            System.out.println("\n" + aggregatorId + ":");
//            System.out.println("  Success rate: " + String.format("%.2f%%", aggregatorSuccessRate) +
//                    " (" + aggregatorVerifiedCount + " / " + aggregatorResults.size() + ")");
//        }
//
//        assertTrue(actualSuccessRate >= expectedSuccessRate,
//                String.format("Expected success rate of at least %d%%, but got %.2f%%",
//                        expectedSuccessRate, actualSuccessRate));
//    }
//
//    @Then("I should see performance metrics for each aggregator")
//    public void iShouldSeePerformanceMetricsForEachAggregator() {
//        List<CommitmentResult> results = helper.collectCommitmentResults();
//        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();
//
//        System.out.println("\n=== 📊 AGGREGATOR PERFORMANCE COMPARISON ===");
//
//        // Print detailed breakdown
//        helper.printDetailedResultsByAggregator(results, aggregatorClients.size());
//
//        // Additional performance analysis
//        helper.printPerformanceComparison(results, aggregatorClients.size());
//    }
//
//    @Then("aggregator performance should meet minimum thresholds")
//    public void aggregatorPerformanceShouldMeetMinimumThresholds() {
//        List<CommitmentResult> results = helper.collectCommitmentResults();
//        List<AggregatorClient> aggregatorClients = context.getAggregatorClients();
//
//        Map<String, List<CommitmentResult>> resultsByAggregator = results.stream()
//                .collect(Collectors.groupingBy(r -> helper.extractAggregatorFromUserName(r.getUserName())));
//
//        for (int i = 0; i < aggregatorClients.size(); i++) {
//            String aggregatorId = "-Aggregator" + i;
//            List<CommitmentResult> aggregatorResults = resultsByAggregator.getOrDefault(aggregatorId, new ArrayList<>());
//
//            if (aggregatorResults.isEmpty()) continue;
//
//            long verifiedCount = aggregatorResults.stream()
//                    .filter(CommitmentResult::isVerified)
//                    .count();
//
//            double successRate = (double) verifiedCount / aggregatorResults.size() * 100;
//
//            // Assert minimum success rate (configurable)
//            assertTrue(successRate >= 90.0,
//                    String.format("Aggregator%d success rate (%.2f%%) should be at least 90%%", i, successRate));
//
//            // Assert reasonable average inclusion time
//            OptionalDouble avgInclusionTime = aggregatorResults.stream()
//                    .filter(CommitmentResult::isVerified)
//                    .mapToDouble(CommitmentResult::getInclusionDurationMillis)
//                    .average();
//
//            if (avgInclusionTime.isPresent()) {
//                assertTrue(avgInclusionTime.getAsDouble() <= 30000, // 30 seconds max
//                        String.format("Aggregator%d average inclusion time (%.2fms) should be under 30 seconds",
//                                i, avgInclusionTime.getAsDouble()));
//            }
//
//            System.out.println("✅ Aggregator" + i + " meets performance thresholds");
//        }
//    }
//}