package com.unicity.sdk.integration;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.predicate.MaskedPredicate;
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
        client = new StateTransitionClient(new AggregatorClient(aggregatorUrl));
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
    
    protected static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}