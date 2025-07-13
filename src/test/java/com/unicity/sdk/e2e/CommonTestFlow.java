package com.unicity.sdk.e2e;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicate;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenFactory;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.utils.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.unicity.sdk.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Common test flows for token operations, matching TypeScript SDK's CommonTestFlow.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class CommonTestFlow {
    private static final byte[] ALICE_SECRET = "Alice".getBytes(StandardCharsets.UTF_8);
    private static final byte[] BOB_SECRET = "Bob".getBytes(StandardCharsets.UTF_8);
    private static final byte[] CAROL_SECRET = "Carol".getBytes(StandardCharsets.UTF_8);
    
    /**
     * Test basic token transfer flow: Alice -> Bob -> Carol
     */
    public static void testTransferFlow(StateTransitionClient client) throws Exception {
        // Alice mints a token
        byte[] aliceNonce = randomBytes(32);
        SigningService aliceSigningService = SigningService.createFromSecret(ALICE_SECRET, aliceNonce).get();
        
        TokenId tokenId = TokenId.create(randomBytes(32));
        TokenType tokenType = TokenType.create(randomBytes(32));
        TokenCoinData coinData = randomCoinData(2);
        
        MaskedPredicate alicePredicate = MaskedPredicate.create(
            tokenId,
            tokenType,
            aliceSigningService,
            HashAlgorithm.SHA256,
            aliceNonce
        ).get(); //correct
        
        DirectAddress aliceAddress = DirectAddress.create(alicePredicate.getReference()).get();
        TokenState aliceTokenState = TokenState.create(alicePredicate, new byte[0]);

        var tokenData = new TestTokenData(randomBytes(32));

        MintTransactionData<ISerializable> mintData = new MintTransactionData<>(
            tokenId,
            tokenType,
            alicePredicate,
            tokenData,
            coinData,
            null, // no data hash
            randomBytes(32) // salt
        );
        
        // Submit mint transaction using StateTransitionClient
        Commitment<MintTransactionData<ISerializable>> mintCommitment = client.submitMintTransaction(mintData).get();
        
        // Wait for inclusion proof
        InclusionProof mintInclusionProof = InclusionProofUtils.waitInclusionProof(client, mintCommitment).get();
        
        // Create mint transaction
        Transaction<MintTransactionData<ISerializable>> mintTx = client.createTransaction(mintCommitment, mintInclusionProof).get();
        Token aliceToken = new Token(aliceTokenState, mintTx);
        
        // Bob prepares to receive the token
        byte[] bobNonce = randomBytes(32);
        SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce).get();
        MaskedPredicate bobPredicate = MaskedPredicate.create(
            aliceToken.getId(),
            aliceToken.getType(),
            bobSigningService,
            HashAlgorithm.SHA256,
            bobNonce
        ).get();
        DirectAddress bobAddress = DirectAddress.create(bobPredicate.getReference()).get();
        
        // Alice transfers to Bob
        String bobCustomData = "Bob's custom data";
        byte[] bobStateData = bobCustomData.getBytes(StandardCharsets.UTF_8);
        DataHash bobDataHash = DataHasher.digest(HashAlgorithm.SHA256, bobStateData);
        
        TransactionData transferData = TransactionData.create(
            aliceTokenState,
            bobAddress.toString(),
            randomBytes(32),
            bobDataHash,
            null
        ).get();
        
        // Submit transfer transaction
        Commitment<TransactionData> transferCommitment = client.submitTransaction(transferData, aliceSigningService).get();
        
        // Wait for inclusion proof
        InclusionProof transferProof = InclusionProofUtils.waitInclusionProof(client, transferCommitment).get();
        
        // Create transfer transaction
        Transaction<TransactionData> transferTx = client.createTransaction(transferCommitment, transferProof).get();
        
        // Bob finalizes the token
        TokenState bobTokenState = TokenState.create(bobPredicate, bobStateData);
        Token bobToken = (Token) client.finishTransaction(
            aliceToken,
            bobTokenState,
            transferTx
        ).get();
        
        // Verify Bob is now the owner
        assertTrue(bobToken.getState().getUnlockPredicate().isOwner(bobSigningService.getPublicKey()).get());
        assertEquals(aliceToken.getId(), bobToken.getId());
        assertEquals(aliceToken.getType(), bobToken.getType());
        
        // Transfer to Carol with UnmaskedPredicate
        byte[] carolNonce = randomBytes(32);
        SigningService carolSigningService = SigningService.createFromSecret(CAROL_SECRET, carolNonce).get();
        DataHash carolRef = UnmaskedPredicate.calculateReference(
            bobToken.getType(),
            carolSigningService.getAlgorithm(),
            carolSigningService.getPublicKey(),
            HashAlgorithm.SHA256
        );
        DirectAddress carolAddress = DirectAddress.create(carolRef).get();
        
        // Bob transfers to Carol (no custom data)
        TransactionData carolTransferData = TransactionData.create(
            bobTokenState,
            carolAddress.toString(),
            randomBytes(32),
            null, // Carol doesn't provide state
            null
        ).get();
        
        Commitment<TransactionData> carolCommitment = client.submitTransaction(carolTransferData, bobSigningService).get();
        InclusionProof carolProof = InclusionProofUtils.waitInclusionProof(client, carolCommitment).get();
        Transaction<TransactionData> carolTransferTx = client.createTransaction(carolCommitment, carolProof).get();
        
        // Carol creates UnmaskedPredicate and finalizes
        UnmaskedPredicate carolPredicate = UnmaskedPredicate.create(
            bobToken.getId(),
            bobToken.getType(),
            carolSigningService,
            HashAlgorithm.SHA256,
            carolNonce
        ).get();
        
        TokenState carolTokenState = TokenState.create(carolPredicate, null);
        Token carolToken = (Token) client.finishTransaction(
            bobToken,
            carolTokenState,
            carolTransferTx
        ).get();
        
        assertEquals(2, carolToken.getTransactions().size());
    }
    
    /**
     * Test offline token transfer flow using commitments
     */
    public static void testOfflineTransferFlow(StateTransitionClient client) throws Exception {
        // Mint initial token
        byte[] aliceNonce = randomBytes(32);
        SigningService aliceSigningService = SigningService.createFromSecret(ALICE_SECRET, aliceNonce).get();
        
        TokenId tokenId = TokenId.create(randomBytes(32));
        TokenType tokenType = TokenType.create(randomBytes(32));
        TokenCoinData coinData = randomCoinData(2);
        
        MaskedPredicate alicePredicate = MaskedPredicate.create(
            tokenId,
            tokenType,
            aliceSigningService,
            HashAlgorithm.SHA256,
            aliceNonce
        ).get();
        
        DirectAddress aliceAddress = DirectAddress.create(alicePredicate.getReference()).get();
        TokenState aliceTokenState = TokenState.create(alicePredicate, new byte[0]);
        
        // Mint token
        ISerializable tokenData = new ISerializable() {
            @Override
            public Object toJSON() {
                return "{}";
            }
            
            @Override
            public byte[] toCBOR() {
                return new byte[0];
            }
        };
        
        MintTransactionData<ISerializable> mintData = new MintTransactionData<>(
            tokenId,
            tokenType,
            alicePredicate,
            tokenData,
            coinData,
            null, // no data hash
            randomBytes(32) // salt
        );
        
        Commitment<MintTransactionData<ISerializable>> mintCommitment = client.submitMintTransaction(mintData).get();
        InclusionProof mintProof = InclusionProofUtils.waitInclusionProof(client, mintCommitment).get();
        Transaction<MintTransactionData<ISerializable>> mintTx = client.createTransaction(mintCommitment, mintProof).get();
        Token token = new Token(aliceTokenState, mintTx);
        
        // Bob prepares to receive
        byte[] bobNonce = randomBytes(32);
        SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce).get();
        MaskedPredicate bobPredicate = MaskedPredicate.create(
            token.getId(),
            token.getType(),
            bobSigningService,
            HashAlgorithm.SHA256,
            bobNonce
        ).get();
        DirectAddress bobAddress = DirectAddress.create(bobPredicate.getReference()).get();
        
        // Create offline commitment
        byte[] customData = "my custom data".getBytes(StandardCharsets.UTF_8);
        TransactionData transactionData = TransactionData.create(
            token.getState(),
            bobAddress.toString(),
            randomBytes(32),
            DataHasher.digest(HashAlgorithm.SHA256, customData),
            "my message".getBytes(StandardCharsets.UTF_8)
        ).get();
        
        Commitment<TransactionData> commitment = client.submitTransaction(transactionData, aliceSigningService).get();
        
        // Simulate offline transfer - Bob receives commitment and token
        // In real world, commitment and token would be serialized to JSON and sent offline
        
        // Bob waits for inclusion proof
        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(client, commitment).get();
        Transaction<TransactionData> confirmedTx = client.createTransaction(commitment, inclusionProof).get();
        
        // Bob finalizes with his predicate
        TokenState bobTokenState = TokenState.create(bobPredicate, customData);
        Token bobToken = (Token) client.finishTransaction(
            token,
            bobTokenState,
            confirmedTx
        ).get();
        
        // Verify ownership transfer
        assertEquals(token.getId(), bobToken.getId());
        assertEquals(token.getType(), bobToken.getType());
    }
    
    // Note: Token splitting functionality is not yet implemented in Java SDK
    // public static void testSplitFlow(StateTransitionClient client) throws Exception {
    //     // TODO: Implement when TokenSplitBuilder is available
    // }
}