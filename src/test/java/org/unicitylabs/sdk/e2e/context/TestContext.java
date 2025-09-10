package org.unicitylabs.sdk.e2e.context;

import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.api.AggregatorClient;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TestUtils;
import org.unicitylabs.sdk.utils.helpers.PendingTransfer;
import io.cucumber.java.en.Given;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Shared test context that maintains state across all step definition classes.
 * This allows different step definition classes to share data and avoid duplication.
 */
public class TestContext {

    // Core clients
    private AggregatorClient aggregatorClient;
    private TestAggregatorClient testAggregatorClient;
    private StateTransitionClient client;

    // User management
    private Map<String, SigningService> userSigningServices = new HashMap<>();
    private Map<String, byte[]> userNonces = new HashMap<>();
    private Map<String, byte[]> userSecrets = new HashMap<>();
    private Map<String, Predicate> userPredicate = new HashMap<>();
    private Map<String, List<Token>> userTokens = new HashMap<>();
    private Map<String, List<Token>> nameTagTokens = new HashMap<>();
    private final Map<String, List<PendingTransfer>> pendingTransfers = new HashMap<>();


    // Test execution state
    private Long blockHeight;
    private byte[] randomSecret;
    private byte[] stateBytes;
    private org.unicitylabs.sdk.hash.DataHash stateHash;
    private org.unicitylabs.sdk.hash.DataHash txDataHash;
    private SubmitCommitmentResponse commitmentResponse;
    private long submissionDuration;
    private Exception lastError;
    private boolean operationSucceeded;

    // Performance testing
    private int configuredThreadCount;
    private int configuredCommitmentsPerThread;
    private List<Future<Boolean>> concurrentResults = new ArrayList<>();
    private long concurrentSubmissionDuration;
    private List<org.unicitylabs.sdk.utils.TestUtils.TokenOperationResult> bulkResults = new ArrayList<>();
    private long bulkOperationDuration;

    // Transfer chain tracking
    private List<String> transferChain = new ArrayList<>();
    private Token chainToken;
    private Map<String, String> transferCustomData = new HashMap<>();

    // Current operation context
    private String currentUser;
    private String expectedErrorType;
    private int expectedSplitCount;
    private int configuredUserCount;
    private int configuredTokensPerUser;


    // Getters and Setters
    public AggregatorClient getAggregatorClient() { return aggregatorClient; }
    public void setAggregatorClient(AggregatorClient aggregatorClient) { this.aggregatorClient = aggregatorClient; }

    public TestAggregatorClient getTestAggregatorClient() { return testAggregatorClient; }
    public void setTestAggregatorClient(TestAggregatorClient testAggregatorClient) { this.testAggregatorClient = testAggregatorClient; }

    public StateTransitionClient getClient() { return client; }
    public void setClient(StateTransitionClient client) { this.client = client; }

    public Map<String, SigningService> getUserSigningServices() { return userSigningServices; }
    public void setUserSigningServices(Map<String, SigningService> userSigningServices) { this.userSigningServices = userSigningServices; }

    public Map<String, byte[]> getUserNonces() { return userNonces; }
    public void setUserNonces(Map<String, byte[]> userNonces) { this.userNonces = userNonces; }

    public Map<String, byte[]> getUserSecret() { return userSecrets; }
    public void setUserSecret(Map<String, byte[]> userSecrets) { this.userSecrets = userSecrets; }

    public Map<String, Predicate> getUserPredicate() {
        return userPredicate;
    }

    public void setUserPredicate(Map<String, Predicate> userPredicate) {
        this.userPredicate = userPredicate;
    }

    public Map<String, List<Token>> getUserTokens() { return userTokens; }
    public void setUserTokens(Map<String, List<Token>> userTokens) { this.userTokens = userTokens; }

    public Map<String, List<Token>> getNameTagTokens() { return nameTagTokens; }
    public void setNameTagTokens(Map<String, List<Token>> nameTagTokens) { this.nameTagTokens = nameTagTokens; }

    public Long getBlockHeight() { return blockHeight; }
    public void setBlockHeight(Long blockHeight) { this.blockHeight = blockHeight; }

    public byte[] getRandomSecret() { return randomSecret; }
    public void setRandomSecret(byte[] randomSecret) { this.randomSecret = randomSecret; }

    public byte[] getStateBytes() { return stateBytes; }
    public void setStateBytes(byte[] stateBytes) { this.stateBytes = stateBytes; }

    public org.unicitylabs.sdk.hash.DataHash getStateHash() { return stateHash; }
    public void setStateHash(org.unicitylabs.sdk.hash.DataHash stateHash) { this.stateHash = stateHash; }

    public org.unicitylabs.sdk.hash.DataHash getTxDataHash() { return txDataHash; }
    public void setTxDataHash(org.unicitylabs.sdk.hash.DataHash txDataHash) { this.txDataHash = txDataHash; }

    public SubmitCommitmentResponse getCommitmentResponse() { return commitmentResponse; }
    public void setCommitmentResponse(SubmitCommitmentResponse commitmentResponse) { this.commitmentResponse = commitmentResponse; }

    public long getSubmissionDuration() { return submissionDuration; }
    public void setSubmissionDuration(long submissionDuration) { this.submissionDuration = submissionDuration; }

    public Exception getLastError() { return lastError; }
    public void setLastError(Exception lastError) { this.lastError = lastError; }

    public boolean isOperationSucceeded() { return operationSucceeded; }
    public void setOperationSucceeded(boolean operationSucceeded) { this.operationSucceeded = operationSucceeded; }

    public int getConfiguredThreadCount() { return configuredThreadCount; }
    public void setConfiguredThreadCount(int configuredThreadCount) { this.configuredThreadCount = configuredThreadCount; }

    public int getConfiguredCommitmentsPerThread() { return configuredCommitmentsPerThread; }
    public void setConfiguredCommitmentsPerThread(int configuredCommitmentsPerThread) { this.configuredCommitmentsPerThread = configuredCommitmentsPerThread; }

    public List<Future<Boolean>> getConcurrentResults() { return concurrentResults; }
    public void setConcurrentResults(List<Future<Boolean>> concurrentResults) { this.concurrentResults = concurrentResults; }

    public long getConcurrentSubmissionDuration() { return concurrentSubmissionDuration; }
    public void setConcurrentSubmissionDuration(long concurrentSubmissionDuration) { this.concurrentSubmissionDuration = concurrentSubmissionDuration; }

    public List<org.unicitylabs.sdk.utils.TestUtils.TokenOperationResult> getBulkResults() { return bulkResults; }
    public void setBulkResults(List<org.unicitylabs.sdk.utils.TestUtils.TokenOperationResult> bulkResults) { this.bulkResults = bulkResults; }

    public long getBulkOperationDuration() { return bulkOperationDuration; }
    public void setBulkOperationDuration(long bulkOperationDuration) { this.bulkOperationDuration = bulkOperationDuration; }

    public List<String> getTransferChain() { return transferChain; }
    public void setTransferChain(List<String> transferChain) { this.transferChain = transferChain; }

    public Token getChainToken() { return chainToken; }
    public void setChainToken(Token chainToken) { this.chainToken = chainToken; }

    public Map<String, String> getTransferCustomData() { return transferCustomData; }
    public void setTransferCustomData(Map<String, String> transferCustomData) { this.transferCustomData = transferCustomData; }

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }

    public String getExpectedErrorType() { return expectedErrorType; }
    public void setExpectedErrorType(String expectedErrorType) { this.expectedErrorType = expectedErrorType; }

    public int getExpectedSplitCount() { return expectedSplitCount; }
    public void setExpectedSplitCount(int expectedSplitCount) { this.expectedSplitCount = expectedSplitCount; }

    public int getConfiguredUserCount() { return configuredUserCount; }
    public void setConfiguredUserCount(int configuredUserCount) { this.configuredUserCount = configuredUserCount; }

    public int getConfiguredTokensPerUser() { return configuredTokensPerUser; }
    public void setConfiguredTokensPerUser(int configuredTokensPerUser) { this.configuredTokensPerUser = configuredTokensPerUser; }

    public void savePendingTransfer(String user, Token token, Transaction<TransferTransactionData> tx) {
        pendingTransfers.computeIfAbsent(user, k -> new ArrayList<>())
                .add(new PendingTransfer(token, tx));
    }

    public List<PendingTransfer> getPendingTransfers(String user) {
        return pendingTransfers.getOrDefault(user, List.of());
    }

    public void clearPendingTransfers(String user) {
        pendingTransfers.remove(user);
    }


    // Utility methods
    public void addUserToken(String userName, Token token) {
        userTokens.computeIfAbsent(userName, k -> new ArrayList<>()).add(token);
    }

    public Token getUserToken(String userName) {
        List<Token> tokens = userTokens.get(userName);
        return (tokens != null && !tokens.isEmpty()) ? tokens.get(0) : null;
    }

    public Token getUserToken(String userName, int index) {
        List<Token> tokens = userTokens.get(userName);
        return (tokens != null && tokens.size() > index) ? tokens.get(index) : null;
    }

    public void addNameTagToken(String userName, Token nameTagToken) {
        nameTagTokens.computeIfAbsent(userName, k -> new ArrayList<>()).add(nameTagToken);
    }

    public Token getNameTagToken(String userName) {
        List<Token> tokens = nameTagTokens.get(userName);
        return (tokens != null && !tokens.isEmpty()) ? tokens.get(0) : null;
    }

    public void clearUserData() {
        userSigningServices.clear();
        userNonces.clear();
        userSecrets.clear();
        userTokens.clear();
        nameTagTokens.clear();
    }

    public void clearTestState() {
        configuredUserCount = 0;
        blockHeight = null;
        randomSecret = null;
        stateBytes = null;
        stateHash = null;
        txDataHash = null;
        commitmentResponse = null;
        submissionDuration = 0;
        lastError = null;
        operationSucceeded = false;
        concurrentResults.clear();
        bulkResults.clear();
        transferChain.clear();
        chainToken = null;
        transferCustomData.clear();
        currentUser = null;
        expectedErrorType = null;
    }

    public void reset() {
        clearUserData();
        clearTestState();
        aggregatorClient = null;
        testAggregatorClient = null;
        client = null;
    }
}