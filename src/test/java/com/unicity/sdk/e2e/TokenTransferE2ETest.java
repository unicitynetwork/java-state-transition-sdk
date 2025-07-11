package com.unicity.sdk.e2e;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.api.AggregatorClient;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicate;
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
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.utils.InclusionProofUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end tests for token transfers including online and offline transfers.
 */
@EnabledIfEnvironmentVariable(named = "AGGREGATOR_URL", matches = ".+")
public class TokenTransferE2ETest {
    
    private static final SecureRandom random = new SecureRandom();
    private static final byte[] ALICE_SECRET = "Alice".getBytes(StandardCharsets.UTF_8);
    private static final byte[] BOB_SECRET = "Bob".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CAROL_SECRET = "Carol".getBytes(StandardCharsets.UTF_8);
    
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
    void testTokenTransferFlow() throws Exception {
        System.out.println("=== Starting Token Transfer Flow Test ===");
        
        // Step 1: Mint token for Alice
        System.out.println("\n1. Minting token for Alice...");
        MintResult mintResult = mintToken(ALICE_SECRET);
        Token<Transaction<MintTransactionData<?>>> aliceToken = mintResult.token;
        
        System.out.println("Token minted successfully!");
        System.out.println("Token ID: " + aliceToken.getId().toJSON());
        System.out.println("Token coins: " + aliceToken.getCoins().getCoins());
        
        // Step 2: Bob prepares to receive the token
        System.out.println("\n2. Bob prepares to receive token...");
        String bobTokenState = "Bob's custom data";
        byte[] bobNonce = new byte[32];
        random.nextBytes(bobNonce);
        
        SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce).get();
        MaskedPredicate bobPredicate = MaskedPredicate.create(
            aliceToken.getId(),
            aliceToken.getType(),
            bobSigningService,
            HashAlgorithm.SHA256,
            bobNonce
        ).get();
        DirectAddress bobAddress = DirectAddress.create(bobPredicate.getReference()).get();
        System.out.println("Bob's address: " + bobAddress.toString());
        
        // Step 3: Alice transfers token to Bob
        System.out.println("\n3. Alice transfers token to Bob...");
        Transaction<TransactionData> transferTx = sendToken(
            aliceToken,
            mintResult.signingService,
            bobAddress,
            bobTokenState
        ).get();
        System.out.println("Transfer transaction completed!");
        
        // Step 4: Bob receives the token
        System.out.println("\n4. Bob receives the token...");
        TokenState bobTokenState_ = TokenState.create(bobPredicate, bobTokenState.getBytes(StandardCharsets.UTF_8));
        Token<Transaction<MintTransactionData<?>>> bobToken = client.finishTransaction(aliceToken, bobTokenState_, transferTx).get();
        
        // Verify ownership
        assertTrue(bobToken.getState().getUnlockPredicate().isOwner(bobSigningService.getPublicKey()).get());
        assertEquals(aliceToken.getId(), bobToken.getId());
        assertEquals(aliceToken.getType(), bobToken.getType());
        assertEquals(aliceToken.getCoins().toJSON(), bobToken.getCoins().toJSON());
        System.out.println("Bob successfully received the token!");
        
        // Step 5: Verify original token has been spent
        System.out.println("\n5. Verifying original token status...");
        InclusionProofVerificationStatus aliceTokenStatus = 
            client.getTokenStatus(aliceToken, mintResult.signingService.getPublicKey()).get();
        assertEquals(InclusionProofVerificationStatus.OK, aliceTokenStatus);
        System.out.println("Original token has been spent (status: OK)");
        
        // Step 6: Bob transfers to Carol with UnmaskedPredicate
        System.out.println("\n6. Bob transfers to Carol with UnmaskedPredicate...");
        byte[] carolNonce = new byte[32];
        random.nextBytes(carolNonce);
        SigningService carolSigningService = SigningService.createFromSecret(CAROL_SECRET, carolNonce).get();
        
        DataHash carolRef = UnmaskedPredicate.calculateReference(
            aliceToken.getType(),
            carolSigningService.getAlgorithm(),
            carolSigningService.getPublicKey(),
            HashAlgorithm.SHA256
        );
        DirectAddress carolAddress = DirectAddress.create(carolRef).get();
        System.out.println("Carol's address: " + carolAddress.toString());
        
        Transaction<TransactionData> txToCarol = sendToken(
            bobToken,
            bobSigningService,
            carolAddress,
            null // Carol doesn't provide state
        ).get();
        
        // Step 7: Carol receives the token
        System.out.println("\n7. Carol receives the token...");
        UnmaskedPredicate carolPredicate = UnmaskedPredicate.create(
            bobToken.getId(),
            bobToken.getType(),
            carolSigningService,
            HashAlgorithm.SHA256,
            carolNonce
        ).get();
        
        TokenState carolTokenState = TokenState.create(carolPredicate, new byte[0]);
        Token<?> carolToken = client.finishTransaction(bobToken, carolTokenState, txToCarol).get();
        
        assertTrue(carolToken.getState().getUnlockPredicate().isOwner(carolSigningService.getPublicKey()).get());
        assertEquals(2, carolToken.getTransactions().size());
        System.out.println("Carol successfully received the token!");
        System.out.println("Total transactions: " + carolToken.getTransactions().size());
        
        System.out.println("\n=== Token Transfer Flow Test Completed Successfully! ===");
    }

    @Test
    void testOfflineTokenTransfer() throws Exception {
        System.out.println("=== Starting Offline Token Transfer Test ===");
        
        // Step 1: Mint token for Alice
        System.out.println("\n1. Minting token for Alice...");
        MintResult mintResult = mintToken(ALICE_SECRET);
        Token<Transaction<MintTransactionData<?>>> token = mintResult.token;
        
        System.out.println("Token minted successfully!");
        System.out.println("Token ID: " + token.getId().toJSON());
        
        // Step 2: Bob prepares to receive token
        System.out.println("\n2. Bob prepares to receive token offline...");
        byte[] bobNonce = new byte[32];
        random.nextBytes(bobNonce);
        SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce).get();
        
        MaskedPredicate bobPredicate = MaskedPredicate.create(
            token.getId(),
            token.getType(),
            bobSigningService,
            HashAlgorithm.SHA256,
            bobNonce
        ).get();
        
        DirectAddress bobAddress = DirectAddress.create(bobPredicate.getReference()).get();
        System.out.println("Bob's address: " + bobAddress.toString());
        
        // Step 3: Alice creates offline transaction
        System.out.println("\n3. Alice creates offline transaction...");
        byte[] salt = new byte[32];
        random.nextBytes(salt);
        
        String bobCustomData = "Bob's offline data";
        DataHash dataHash = DataHasher.digest(
            HashAlgorithm.SHA256, 
            bobCustomData.getBytes(StandardCharsets.UTF_8)
        );
        
        TransactionData transactionData = TransactionData.create(
            token.getState(),
            bobAddress.toString(),
            salt,
            dataHash,
            "Offline transfer message".getBytes(StandardCharsets.UTF_8),
            null
        ).get();
        
        // Create commitment
        Authenticator authenticator = Authenticator.create(
            mintResult.signingService,
            transactionData.getHash(),
            transactionData.getSourceState().getHash()
        ).get();
        
        RequestId requestId = RequestId.create(
            mintResult.signingService.getPublicKey(), 
            transactionData.getSourceState().getHash()
        ).get();
        
        Commitment<TransactionData> commitment = new Commitment<>(requestId, transactionData, authenticator);
        
        System.out.println("Offline commitment created");
        System.out.println("Request ID: " + requestId.toJSON());
        
        // Step 4: Simulate offline transfer (in real scenario, this would be sent via file/QR code/etc)
        System.out.println("\n4. Simulating offline transfer...");
        // In reality, Alice would send the commitment and token data to Bob offline
        
        // Step 5: Bob submits the commitment online
        System.out.println("\n5. Bob submits commitment online...");
        var response = client.submitCommitment(commitment).get();
        assertEquals(SubmitCommitmentStatus.SUCCESS, response.getStatus());
        System.out.println("Commitment submitted successfully!");
        
        // Step 6: Bob waits for inclusion proof
        System.out.println("\n6. Bob waits for inclusion proof...");
        var inclusionProof = InclusionProofUtils.waitInclusionProof(client, commitment).get();
        Transaction<TransactionData> confirmedTx = client.createTransaction(commitment, inclusionProof).get();
        System.out.println("Transaction confirmed!");
        
        // Step 7: Bob completes the transaction
        System.out.println("\n7. Bob completes the transaction...");
        TokenState bobTokenState = TokenState.create(bobPredicate, bobCustomData.getBytes(StandardCharsets.UTF_8));
        @SuppressWarnings("unchecked")
        Token<Transaction<MintTransactionData<?>>> bobToken = (Token<Transaction<MintTransactionData<?>>>) 
            client.finishTransaction(token, bobTokenState, confirmedTx).get();
        
        // Verify ownership
        assertTrue(bobToken.getState().getUnlockPredicate().isOwner(bobSigningService.getPublicKey()).get());
        assertEquals(token.getId(), bobToken.getId());
        assertEquals(token.getType(), bobToken.getType());
        assertEquals(token.getCoins().toJSON(), bobToken.getCoins().toJSON());
        
        System.out.println("Bob successfully received the token offline!");
        
        // Verify original token has been spent
        System.out.println("\n8. Verifying original token status...");
        InclusionProofVerificationStatus mintedTokenStatus = 
            client.getTokenStatus(token, mintResult.signingService.getPublicKey()).get();
        assertEquals(InclusionProofVerificationStatus.OK, mintedTokenStatus);
        System.out.println("Original token has been spent (status: OK)");
        
        System.out.println("\n=== Offline Token Transfer Test Completed Successfully! ===");
    }

    private MintResult mintToken(byte[] secret) throws Exception {
        // Create token ID and type
        byte[] tokenIdData = new byte[32];
        random.nextBytes(tokenIdData);
        TokenId tokenId = TokenId.create(tokenIdData);

        byte[] tokenTypeData = new byte[32];
        random.nextBytes(tokenTypeData);
        TokenType tokenType = TokenType.create(tokenTypeData);

        // Create coin data
        byte[] coinId1Data = new byte[32];
        byte[] coinId2Data = new byte[32];
        random.nextBytes(coinId1Data);
        random.nextBytes(coinId2Data);
        
        Map<CoinId, BigInteger> coins = new HashMap<>();
        coins.put(new CoinId(coinId1Data), BigInteger.valueOf(50));
        coins.put(new CoinId(coinId2Data), BigInteger.valueOf(50));
        TokenCoinData coinData = TokenCoinData.create(coins);

        // Create predicate
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
        
        // Create empty token data
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
        var inclusionProof = InclusionProofUtils.waitInclusionProof(client, commitment).get();
        var mintTransaction = client.createTransaction(commitment, inclusionProof).get();

        // Create token
        TokenState tokenState = TokenState.create(predicate, data);
        @SuppressWarnings("unchecked")
        Token<Transaction<MintTransactionData<?>>> token = new Token<>(
            tokenState, 
            (Transaction<MintTransactionData<?>>) mintTransaction
        );
        
        return new MintResult(token, signingService, nonce);
    }

    private CompletableFuture<Transaction<TransactionData>> sendToken(
            Token<Transaction<MintTransactionData<?>>> token,
            SigningService signingService,
            DirectAddress recipient,
            String tokenState) {
        
        CompletableFuture<DataHash> stateHashFuture;
        if (tokenState != null) {
            stateHashFuture = CompletableFuture.completedFuture(
                DataHasher.digest(HashAlgorithm.SHA256, tokenState.getBytes(StandardCharsets.UTF_8))
            );
        } else {
            stateHashFuture = CompletableFuture.completedFuture(null);
        }

        byte[] salt = new byte[32];
        random.nextBytes(salt);
        byte[] message = "Transfer message".getBytes(StandardCharsets.UTF_8);

        return stateHashFuture.thenCompose(stateHash -> 
            TransactionData.create(
                token.getState(),
                recipient.toString(),
                salt,
                stateHash,
                message,
                null
            ).thenCompose(transactionData -> 
                client.submitTransaction(transactionData, signingService)
                    .thenCompose(commitment -> {
                        if (commitment.getAuthenticator() == null) {
                            return CompletableFuture.failedFuture(
                                new RuntimeException("Failed to submit transaction commitment")
                            );
                        }
                        
                        return InclusionProofUtils.waitInclusionProof(client, commitment)
                            .thenCompose(inclusionProof -> 
                                client.createTransaction(commitment, inclusionProof)
                            );
                    })
            )
        );
    }
    
    private static class MintResult {
        final Token<Transaction<MintTransactionData<?>>> token;
        final SigningService signingService;
        final byte[] nonce;
        
        MintResult(Token<Transaction<MintTransactionData<?>>> token, SigningService signingService, byte[] nonce) {
            this.token = token;
            this.signingService = signingService;
            this.nonce = nonce;
        }
    }
}