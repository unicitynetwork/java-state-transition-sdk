package com.unicity.sdk.e2e.steps.shared;

import com.fasterxml.jackson.core.type.TypeReference;
import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.TestAggregatorClient;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.e2e.config.CucumberConfiguration;
import com.unicity.sdk.e2e.context.TestContext;
import com.unicity.sdk.predicate.UnmaskedPredicate;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.transaction.*;
import com.unicity.sdk.utils.TestUtils;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicateReference;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.util.InclusionProofUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.unicity.sdk.utils.TestUtils.randomBytes;
import static com.unicity.sdk.utils.TestUtils.randomCoinData;
import static org.junit.jupiter.api.Assertions.*;

import com.unicity.sdk.e2e.steps.shared.StepHelper;


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
        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");
        context.setAggregatorClient(new AggregatorClient(aggregatorUrl));
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

        SigningService signingService = SigningService.createFromSecret(context.getRandomSecret(), null);
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
    }

    @When("I submit all commitments concurrently")
    public void iSubmitAllCommitmentsConcurrently() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(context.getConfiguredThreadCount());
        CountDownLatch latch = new CountDownLatch(
                context.getConfiguredThreadCount() * context.getConfiguredCommitmentsPerThread()
        );
        List<Future<Boolean>> results = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        for (int t = 0; t < context.getConfiguredThreadCount(); t++) {
            for (int c = 0; c < context.getConfiguredCommitmentsPerThread(); c++) {
                results.add(executor.submit(() -> {
                    try {
                        return helper.submitSingleCommitment();
                    } finally {
                        latch.countDown();
                    }
                }));
            }
        }

        latch.await();
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        context.setConcurrentResults(results);
        context.setConcurrentSubmissionDuration(endTime - startTime);
    }

    @Then("all commitments should be submitted successfully")
    public void allCommitmentsShouldBeSubmittedSuccessfully() throws Exception {
        int expectedTotal = context.getConfiguredThreadCount() * context.getConfiguredCommitmentsPerThread();
        long successCount = context.getConcurrentResults().stream()
                .filter(f -> {
                    try { return f.get(); } catch (Exception e) { return false; }
                })
                .count();

        assertEquals(expectedTotal, successCount, "All commitments should succeed");
        System.out.println("Total commitments: " + expectedTotal);
        System.out.println("Successful: " + successCount);
    }

    @And("all submissions should complete within a reasonable time")
    public void allSubmissionsShouldCompleteWithinAReasonableTime() {
        System.out.println("Concurrent submission took: " + context.getConcurrentSubmissionDuration() + " ms");
        assertTrue(context.getConcurrentSubmissionDuration() < 30000,
                "Concurrent submissions should complete in reasonable time");
    }

    // Token Operations
    @Given("{string} mints a token with random coin data")
    public void userMintsATokenWithRandomCoinData(String userName) throws Exception {
        TokenId tokenId = TestUtils.generateRandomTokenId();
        TokenType tokenType = TestUtils.generateRandomTokenType();
        TokenCoinData coinData = randomCoinData(2);

        Token token = TestUtils.mintTokenForUser(
                context.getClient(),
                context.getUserSigningServices().get(userName),
                context.getUserNonces().get(userName),
                tokenId,
                tokenType,
                coinData
        );

        context.addUserToken(userName, token);
        context.setCurrentUser(userName);
    }

    @When("{string} transfers the token to {string} using a proxy address")
    public void userTransfersTheTokenToUserUsingAProxyAddress(String fromUser, String toUser) throws Exception {
        // Create nametag token for recipient
        Token nameTagToken = helper.createNameTagTokenForUser(
                toUser,
                context.getUserToken(fromUser).getType(),
                java.util.UUID.randomUUID().toString(),
                "test"
        );
        context.addNameTagToken(toUser, nameTagToken);

        Token sourceToken = context.getUserToken(fromUser);

        ProxyAddress proxyAddress = ProxyAddress.create(nameTagToken.getId());

        String customData = "Transfer from " + fromUser + " to " + toUser;
        helper.transferToken(fromUser, toUser, sourceToken, proxyAddress, customData);
    }

    @When("{string} transfers the token to {string} using an unmasked predicate")
    public void userTransfersTheTokenToUserUsingAnUnmaskedPredicate(String fromUser, String toUser) throws Exception {
        Token sourceToken = context.getUserToken(fromUser);
        SigningService toSigningService = context.getUserSigningServices().get(toUser);

        UnmaskedPredicate userPredicate = UnmaskedPredicate.create(
                toSigningService,
                HashAlgorithm.SHA256,
                context.getUserNonces().get(toUser)
        );
        context.getUserPredicate().put(toUser, userPredicate);

        DirectAddress toAddress = userPredicate.getReference(sourceToken.getType()).toAddress();

        helper.transferToken(fromUser, toUser, sourceToken, toAddress, null);
    }

    @And("{string} finalizes the token with custom data {string}")
    public void userFinalizesTheTokenWithCustomData(String userName, String customData) {
        Token token = context.getUserToken(userName);
        assertNotNull(token, userName + " should have received the token");

        // Verify that the token state contains the expected custom data
        if (token.getState().getData().isPresent() && customData != null && !customData.isEmpty()) {
            byte[] actualData = token.getState().getData().get();
            String actualCustomData = new String(actualData, StandardCharsets.UTF_8);
            assertTrue(actualCustomData.contains(userName), "Token should contain data related to " + userName);
        } else if (customData != null && !customData.isEmpty()) {
            fail("Token should contain custom data but none was found");
        }
    }

    @And("{string} finalizes the token without custom data")
    public void userFinalizesTheTokenWithoutCustomData(String userName) {
        Token token = context.getUserToken(userName);
        assertNotNull(token, userName + " should have received the token");
    }

    @Then("{string} should own the token successfully")
    public void userShouldOwnTheTokenSuccessfully(String userName) {
        Token token = context.getUserToken(userName);
        context.setCurrentUser(userName);
        SigningService signingService = context.getUserSigningServices().get(userName);

        assertTrue(token.verify().isSuccessful(), "Token should be valid");
        assertTrue(token.getState().getUnlockPredicate().isOwner(signingService.getPublicKey()),
                userName + " should own the token");
    }
}