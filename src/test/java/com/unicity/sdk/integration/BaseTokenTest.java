package com.unicity.sdk.integration;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.utils.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base class for token integration tests.
 * Contains shared testing logic for both Docker-based and E2E tests.
 */
@Tag("integration")
public abstract class BaseTokenTest {
    protected static final Logger logger = LoggerFactory.getLogger(BaseTokenTest.class);
    protected static final byte[] ownerSecret = "test-secret".getBytes(StandardCharsets.UTF_8);
    protected static final SecureRandom random = new SecureRandom();
    
    protected StateTransitionClient client;
    
    /**
     * Get the aggregator URL for testing.
     * Subclasses should implement this to provide the appropriate URL.
     */
    protected abstract String getAggregatorUrl();
    
    /**
     * Get the timeout duration for waiting for inclusion proofs.
     * Can be overridden by subclasses to provide different timeouts.
     */
    protected Duration getInclusionProofTimeout() {
        return Duration.ofSeconds(10);
    }
    
    /**
     * Initialize the client. Called from @BeforeAll in subclasses.
     */
    protected void initializeClient() {
        String aggregatorUrl = getAggregatorUrl();
        logger.info("Initializing client with aggregator URL: {}", aggregatorUrl);
        AggregatorClient aggregatorClient = new AggregatorClient(aggregatorUrl);
        client = new StateTransitionClient(aggregatorClient);
    }
    
    @Test
    @Order(1)
    void testAggregatorIsRunning() throws Exception {
        String aggregatorUrl = getAggregatorUrl();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(aggregatorUrl + "/").openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        int code = conn.getResponseCode();
        assertTrue(code == 200 || code == 404 || code == 400, "Aggregator should respond");
        logger.info("Aggregator health check passed with response code: {}", code);
    }
    
    @Test
    @Order(2)
    void testGetBlockHeight() throws Exception {
        Long blockHeight = client.getAggregatorClient().getBlockHeight().get();
        assertNotNull(blockHeight);
        assertTrue(blockHeight >= 0);
        logger.info("Current block height: {}", blockHeight);
    }
    
    @Test
    @Order(3)
    void testMintToken() throws Exception {
        logger.info("Starting token minting test");
        
        // Create token mint data
        TokenId tokenId = TokenId.create(randomBytes(32));
        TokenType tokenType = TokenType.create(randomBytes(32));
        TestTokenData tokenData = new TestTokenData(randomBytes(32));
        
        // Create coins
        Map<CoinId, BigInteger> coins = new HashMap<>();
        coins.put(new CoinId(randomBytes(32)), BigInteger.valueOf(100));
        coins.put(new CoinId(randomBytes(32)), BigInteger.valueOf(50));
        TokenCoinData coinData = new TokenCoinData(coins);
        
        byte[] salt = randomBytes(32);
        byte[] nonce = randomBytes(32);
        
        // Create predicate
        SigningService signingService = SigningService.createFromSecret(ownerSecret, nonce).get();
        MaskedPredicate predicate = MaskedPredicate.create(
            tokenId,
            tokenType,
            signingService,
            HashAlgorithm.SHA256,
            nonce
        ).get();
        
        // Note: dataHash field is optional for mint transactions (can be null)
        MintTransactionData<TestTokenData> mintData = new MintTransactionData<>(
            tokenId,
            tokenType,
            predicate,
            tokenData,
            coinData,
            null,  // dataHash (optional)
            salt
        );
        
        logger.info("Submitting mint transaction for token ID: {}", tokenId.toJSON());
        logger.info("MintTransactionData JSON: {}", mintData.toJSON());
        logger.info("MintTransactionData hash: {}", mintData.getHash().toJSON());
        
        // Submit mint transaction
        Commitment<MintTransactionData<TestTokenData>> commitment =
            client.submitMintTransaction(mintData).get();
        
        assertNotNull(commitment);
        logger.info("Mint transaction submitted, waiting for inclusion proof");
        
        // Wait for inclusion proof with configurable timeout
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
            client, 
            commitment,
            getInclusionProofTimeout(),
            Duration.ofSeconds(1)  // polling interval
        ).get();
        assertNotNull(inclusionProof);
        
        // Create transaction
        Transaction<MintTransactionData<TestTokenData>> mintTransaction =
            client.createTransaction(commitment, inclusionProof).get();
        assertNotNull(mintTransaction);
        
        // Create token state with tokenData bytes
        TokenState tokenState = TokenState.create(predicate, tokenData.getData());
        
        // Create token
        Token<Transaction<MintTransactionData<?>>> token =
            new Token<>(tokenState, (Transaction) mintTransaction);
        
        assertEquals(tokenId, token.getId());
        assertEquals(tokenType, token.getType());
        assertEquals(tokenData, token.getData());
        assertNotNull(token.getCoins());
        assertEquals(2, token.getCoins().getCoins().size());
        
        logger.info("Token minted successfully!");
    }
    
    @Test
    @Order(4)
    void testOfflineTransferFlow() throws Exception {
        logger.info("Starting offline transfer flow test");
        
        // Step 1: Mint a token to the initial owner
        TokenId tokenId = TokenId.create(randomBytes(32));
        TokenType tokenType = TokenType.create(randomBytes(32));
        TestTokenData tokenData = new TestTokenData(randomBytes(32));
        
        // Create coins
        Map<CoinId, BigInteger> coins = new HashMap<>();
        coins.put(new CoinId(randomBytes(32)), BigInteger.valueOf(100));
        coins.put(new CoinId(randomBytes(32)), BigInteger.valueOf(50));
        TokenCoinData coinData = new TokenCoinData(coins);
        
        byte[] salt = randomBytes(32);
        byte[] initialOwnerNonce = randomBytes(32);
        
        // Create initial owner predicate
        SigningService initialOwnerSigningService = SigningService.createFromSecret(ownerSecret, initialOwnerNonce).get();
        MaskedPredicate initialOwnerPredicate = MaskedPredicate.create(
            tokenId,
            tokenType,
            initialOwnerSigningService,
            HashAlgorithm.SHA256,
            initialOwnerNonce
        ).get();
        
        // Create mint transaction
        MintTransactionData<TestTokenData> mintData = new MintTransactionData<>(
            tokenId,
            tokenType,
            initialOwnerPredicate,
            tokenData,
            coinData,
            null,  // dataHash (optional)
            salt
        );
        
        logger.info("Minting token for offline transfer test");
        
        // Submit mint transaction
        Commitment<MintTransactionData<TestTokenData>> commitment =
            client.submitMintTransaction(mintData).get();
        
        // Wait for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
            client, 
            commitment,
            getInclusionProofTimeout(),
            Duration.ofSeconds(1)
        ).get();
        
        // Create transaction
        Transaction<MintTransactionData<TestTokenData>> mintTransaction =
            client.createTransaction(commitment, inclusionProof).get();
        
        // Create token
        TokenState tokenState = TokenState.create(initialOwnerPredicate, tokenData.getData());
        Token<Transaction<MintTransactionData<?>>> mintedToken = new Token<>(tokenState, (Transaction) mintTransaction);
        
        // Step 2: Prepare recipient
        byte[] recipientSecret = "recipient-secret".getBytes(StandardCharsets.UTF_8);
        byte[] recipientNonce = randomBytes(32);
        SigningService recipientSigningService = SigningService.createFromSecret(recipientSecret, recipientNonce).get();
        MaskedPredicate recipientPredicate = MaskedPredicate.create(
            mintedToken.getId(),
            mintedToken.getType(),
            recipientSigningService,
            HashAlgorithm.SHA256,
            recipientNonce
        ).get();
        
        DirectAddress recipientAddress = DirectAddress.create(recipientPredicate.getReference()).get();
        logger.info("Recipient address for offline transfer: {}", recipientAddress.toString());
        
        // Step 3: Create offline transaction
        // The recipient's custom data that will be used in the new token state
        byte[] recipientCustomData = "recipient's custom data".getBytes(StandardCharsets.UTF_8);
        DataHash dataHash = new JavaDataHasher(HashAlgorithm.SHA256)
            .update(recipientCustomData)
            .digest()
            .get();
        
        TransactionData transactionData = TransactionData.create(
            mintedToken.getState(),
            recipientAddress.toString(),
            randomBytes(32),  // salt
            dataHash,
            "offline transfer message".getBytes(StandardCharsets.UTF_8)
        ).get();
        
        // Create offline commitment (similar to online but not submitted immediately)
        Authenticator authenticator = Authenticator.create(
            initialOwnerSigningService,
            transactionData.getHash(),
            transactionData.getSourceState().getHash()
        ).get();
        
        RequestId requestId = RequestId.create(
            initialOwnerSigningService.getPublicKey(), 
            transactionData.getSourceState().getHash()
        ).get();
        
        Commitment<TransactionData> offlineCommitment = new Commitment<>(requestId, transactionData, authenticator);
        
        // In a real scenario, the commitment would be serialized and transferred offline
        logger.info("Offline commitment created for request ID: {}", requestId.toJSON());
        
        // Step 4: Simulate offline transfer (in real usage, this would be via NFC, QR code, etc.)
        logger.info("Simulating offline transfer of commitment");
        
        // Step 5: Recipient submits the commitment online
        logger.info("Recipient submitting offline commitment online");
        var response = client.submitCommitment(offlineCommitment).get();
        assertEquals(SubmitCommitmentStatus.SUCCESS, response.getStatus());
        
        // Wait for inclusion proof
        InclusionProof offlineInclusionProof = InclusionProofUtils.waitInclusionProof(
            client, 
            offlineCommitment,
            getInclusionProofTimeout(),
            Duration.ofSeconds(1)
        ).get();
        
        Transaction<TransactionData> confirmedTransaction = client.createTransaction(offlineCommitment, offlineInclusionProof).get();
        
        // Create recipient's token state
        TokenState recipientTokenState = TokenState.create(recipientPredicate, recipientCustomData);
        
        // Finish the transaction
        Token<?> updatedToken = client.finishTransaction(
            mintedToken,
            recipientTokenState,
            confirmedTransaction
        ).get();
        
        // Verify the token is now owned by the recipient
        assertTrue(updatedToken.getState().getUnlockPredicate().isOwner(recipientSigningService.getPublicKey()).get());
        assertEquals(mintedToken.getId(), updatedToken.getId());
        assertEquals(mintedToken.getType(), updatedToken.getType());
        assertArrayEquals(recipientCustomData, updatedToken.getState().getData());
        
        logger.info("Offline transfer completed successfully");
        
        // Step 6: Verify the original token has been spent
        InclusionProofVerificationStatus originalTokenStatus = 
            client.getTokenStatus(mintedToken, initialOwnerSigningService.getPublicKey()).get();
        
        assertEquals(InclusionProofVerificationStatus.OK, originalTokenStatus);
        logger.info("Original token confirmed as spent");
    }
    
    // Removed processReceivedOfflineTransaction method as it used removed offline transaction classes
    
    protected static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}