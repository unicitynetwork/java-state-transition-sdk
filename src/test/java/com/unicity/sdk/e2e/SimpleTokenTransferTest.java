package com.unicity.sdk.e2e;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.utils.InclusionProofUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple token transfer test to verify basic functionality.
 */
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class SimpleTokenTransferTest {
    
    private static final SecureRandom random = new SecureRandom();
    private AggregatorClient aggregatorClient;
    private StateTransitionClient client;

    @BeforeEach
    void setUp() {
        String aggregatorUrl = System.getenv("AGGREGATOR_URL");
        assertNotNull(aggregatorUrl, "AGGREGATOR_URL environment variable must be set");
        
        aggregatorClient = new AggregatorClient(aggregatorUrl);
        client = new StateTransitionClient(aggregatorClient);
    }

    @Test
    void testMintToken() throws Exception {
        // Create token ID and type
        byte[] tokenIdData = new byte[32];
        random.nextBytes(tokenIdData);
        TokenId tokenId = TokenId.create(tokenIdData);

        byte[] tokenTypeData = new byte[32];
        random.nextBytes(tokenTypeData);
        TokenType tokenType = TokenType.create(tokenTypeData);

        // Create coin data
        byte[] coinId1Data = new byte[32];
        random.nextBytes(coinId1Data);
        CoinId coinId1 = new CoinId(coinId1Data);
        
        Map<CoinId, BigInteger> coins = new HashMap<>();
        coins.put(coinId1, BigInteger.valueOf(100));
        TokenCoinData coinData = TokenCoinData.create(coins);

        // Create predicate
        byte[] secret = "Alice".getBytes(StandardCharsets.UTF_8);
        byte[] nonce = new byte[32];
        random.nextBytes(nonce);
        
        SigningService signingService = SigningService.createFromSecret(secret, nonce).get();
        MaskedPredicate predicate = MaskedPredicate.create(
            tokenId, 
            tokenType, 
            signingService, 
            HashAlgorithm.SHA256, 
            nonce
        ).get();

        // Create mint transaction data
        DirectAddress address = DirectAddress.create(predicate.getReference()).get();
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        
        byte[] data = new byte[32];
        random.nextBytes(data);
        
        DataHash dataHash = DataHasher.digest(HashAlgorithm.SHA256, data);
        
        // Create a simple ISerializable wrapper for empty token data
        ISerializable emptyTokenData = new ISerializable() {
            @Override
            public Object toJSON() {
                return "";
            }
            
            @Override
            public byte[] toCBOR() {
                return new byte[0];
            }
        };
        
        MintTransactionData<?> mintData = new MintTransactionData<>(
            tokenId,
            tokenType,
            predicate,
            emptyTokenData,
            coinData,
            dataHash,
            salt
        );

        // Submit mint transaction
        var commitment = client.submitMintTransaction(mintData).get();
        System.out.println("Mint commitment submitted");

        // Wait for inclusion proof
        var inclusionProof = InclusionProofUtils.waitInclusionProof(client, commitment).get();
        System.out.println("Inclusion proof received");

        // Create transaction
        var mintTransaction = client.createTransaction(commitment, inclusionProof).get();
        System.out.println("Mint transaction created");

        // Create token
        TokenState tokenState = TokenState.create(predicate, data);
        @SuppressWarnings("unchecked")
        Token<Transaction<MintTransactionData<?>>> token = new Token<>(
            tokenState, 
            (Transaction<MintTransactionData<?>>) mintTransaction
        );
        
        assertNotNull(token);
        assertEquals(tokenId, token.getId());
        assertEquals(tokenType, token.getType());
        
        System.out.println("Token minted successfully!");
        System.out.println("Token ID: " + token.getId().toJSON());
        System.out.println("Token coins: " + token.getCoins().getCoins());
    }
}