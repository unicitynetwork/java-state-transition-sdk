package com.unicity.sdk.integration;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
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
import com.unicity.sdk.utils.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for token state transitions using Testcontainers.
 * Tests the Java SDK against the aggregator running in Docker.
 * 
 * Run with: ./gradlew test --tests "com.unicity.sdk.integration.*" -Pintegration
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("integration")
public class TokenIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenIntegrationTest.class);
    private static final byte[] ownerSecret = "secret".getBytes(StandardCharsets.UTF_8);
    private static final SecureRandom random = new SecureRandom();
    
    private Network network;
    private GenericContainer<?> mongo1;
    private GenericContainer<?> mongo2;
    private GenericContainer<?> mongo3;
    private GenericContainer<?> mongoSetup;
    private GenericContainer<?> aggregator;
    private StateTransitionClient client;
    
    @BeforeAll
    void setUp() throws Exception {
        network = Network.newNetwork();
        
        // Start MongoDB replica set
        mongo1 = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withNetworkAliases("mongo1")
                .withCommand("--replSet", "rs0", "--bind_ip_all")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        mongo2 = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withNetworkAliases("mongo2")
                .withCommand("--replSet", "rs0", "--bind_ip_all")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        mongo3 = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withNetworkAliases("mongo3")
                .withCommand("--replSet", "rs0", "--bind_ip_all")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        
        mongo1.start();
        mongo2.start();
        mongo3.start();
        
        // Wait for MongoDB to be ready
        Thread.sleep(5000);
        
        // Initialize replica set
        mongoSetup = new GenericContainer<>(DockerImageName.parse("mongo:7.0"))
                .withNetwork(network)
                .withCopyFileToContainer(
                        org.testcontainers.utility.MountableFile.forClasspathResource("docker/aggregator/mongo-init.js"),
                        "/mongo-init.js")
                .withCommand("mongosh", "--host", "mongo1:27017", "--file", "/mongo-init.js");
        
        mongoSetup.start();
        
        // Start aggregator
        aggregator = new GenericContainer<>(DockerImageName.parse("ghcr.io/unicitynetwork/aggregators_net:bbabb5f093e829fa789ed6e83f57af98df3f1752"))
                .withNetwork(network)
                .withNetworkAliases("aggregator-test")
                .withExposedPorts(3000)
                .withEnv("MONGODB_URI", "mongodb://mongo1:27017")
                .withEnv("USE_MOCK_ALPHABILL", "true")
                .withEnv("ALPHABILL_PRIVATE_KEY", "FF00000000000000000000000000000000000000000000000000000000000000")
                .withEnv("DISABLE_HIGH_AVAILABILITY", "true")
                .withEnv("PORT", "3000")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(java.time.Duration.ofMinutes(2)));
        aggregator.start();
        
        // Create client
        Integer mappedPort = aggregator.getMappedPort(3000);
        String aggregatorUrl = "http://localhost:" + mappedPort;
        logger.info("Aggregator URL: {}", aggregatorUrl);
        client = new StateTransitionClient(new AggregatorClient(aggregatorUrl));
    }
    
    @AfterAll
    void tearDown() {
        if (aggregator != null) aggregator.stop();
        if (mongoSetup != null) mongoSetup.stop();
        if (mongo1 != null) mongo1.stop();
        if (mongo2 != null) mongo2.stop();
        if (mongo3 != null) mongo3.stop();
        if (network != null) network.close();
    }
    
    @Test
    @Order(1)
    void testAggregatorIsRunning() throws Exception {
        Integer mappedPort = aggregator.getMappedPort(3000);
        String url = "http://localhost:" + mappedPort + "/";
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        int code = conn.getResponseCode();
        assertTrue(code == 200 || code == 404 || code == 400, "Aggregator should respond");
        logger.info("Aggregator health check passed with response code: {}", code);
    }
    
    @Test
    @Order(2)
    void testGetBlockHeight() throws Exception {
        Thread.sleep(2000);
        Long blockHeight = client.getAggregatorClient().getBlockHeight().get();
        assertNotNull(blockHeight);
        assertTrue(blockHeight > 0);
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
        // The data field was removed from MintTransactionData in the updated structure
        
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
        
        // Wait for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(client, commitment).get();
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
    
    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }
}