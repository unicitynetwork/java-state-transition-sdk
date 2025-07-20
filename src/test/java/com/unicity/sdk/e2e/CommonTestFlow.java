package com.unicity.sdk.e2e;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicate;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.utils.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
//    public static void testTransferFlow(StateTransitionClient client) throws Exception {
//        // Alice mints a token
//        byte[] aliceNonce = randomBytes(32);
//        SigningService aliceSigningService = SigningService.createFromSecret(ALICE_SECRET, aliceNonce);
//
//        TokenId tokenId = new TokenId(randomBytes(32));
//        TokenType tokenType = new TokenType(randomBytes(32));
//        TokenCoinData coinData = randomCoinData(2);
//
//        MaskedPredicate alicePredicate = MaskedPredicate.create(
//            tokenId,
//            tokenType,
//            aliceSigningService,
//            HashAlgorithm.SHA256,
//            aliceNonce
//        ); //correct
//
//        DirectAddress aliceAddress = alicePredicate.getReference().toAddress();
//        TokenState aliceTokenState = TokenState.create(alicePredicate, new byte[0]);
//
//        var tokenData = new TestTokenData(randomBytes(32));
//
//        MintTransactionData<?> mintData = MintTransactionData.create(
//            tokenId,
//            tokenType,
//            tokenData.getData(),
//            coinData,
//            DirectAddress.create(new DataHash(HashAlgorithm.SHA256,new byte[10])),
//            new byte[5],
//            null,
//            null
//        );
//
//        // Submit mint transaction using StateTransitionClient
//        Commitment<MintTransactionData<?>> mintCommitment = client.submitMintTransaction(mintData).get();
//
//        // Wait for inclusion proof
//        InclusionProof mintInclusionProof = InclusionProofUtils.waitInclusionProof(client, mintCommitment).get();
//
//        // Create mint transaction
//        Transaction<MintTransactionData<?>> mintTx = client.createTransaction(mintCommitment, mintInclusionProof).get();
//        Token aliceToken = new Token(aliceTokenState, mintTx);
//
//        // Bob prepares to receive the token
//        byte[] bobNonce = randomBytes(32);
//        SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce);
//        MaskedPredicate bobPredicate = MaskedPredicate.create(
//            aliceToken.getId(),
//            aliceToken.getType(),
//            bobSigningService,
//            HashAlgorithm.SHA256,
//            bobNonce
//        );
//        DirectAddress bobAddress = bobPredicate.getReference().toAddress();
//
//        // Alice transfers to Bob
//        String bobCustomData = "Bob's custom data";
//        byte[] bobStateData = bobCustomData.getBytes(StandardCharsets.UTF_8);
//        DataHash bobDataHash = new DataHasher(HashAlgorithm.SHA256).update(bobStateData).digest();
//
//        TransferTransactionData transferData = TransferTransactionData.create(
//            aliceTokenState,
//            bobAddress,
//            randomBytes(32),
//            bobDataHash,
//            null,
//            List.of()
//        );
//
//        // Submit transfer transaction
//        Commitment<TransferTransactionData> transferCommitment = client.submitTransaction(transferData, aliceSigningService).get();
//
//        // Wait for inclusion proof
//        InclusionProof transferProof = InclusionProofUtils.waitInclusionProof(client, transferCommitment).get();
//
//        // Create transfer transaction
//        Transaction<TransferTransactionData> transferTx = client.createTransaction(transferCommitment, transferProof).get();
//
//        // Bob finalizes the token
//        TokenState bobTokenState = TokenState.create(bobPredicate, bobStateData);
//        Token bobToken = client.finishTransaction(
//            aliceToken,
//            bobTokenState,
//            transferTx
//        );
//
//        // Verify Bob is now the owner
//        assertTrue(bobToken.getState().getUnlockPredicate().isOwner(bobSigningService.getPublicKey()));
//        assertEquals(aliceToken.getId(), bobToken.getId());
//        assertEquals(aliceToken.getType(), bobToken.getType());
//
//        // Transfer to Carol with UnmaskedPredicate
//        byte[] carolNonce = randomBytes(32);
//        SigningService carolSigningService = SigningService.createFromSecret(CAROL_SECRET, carolNonce);
//        DataHash carolRef = UnmaskedPredicate.calculateReference(
//            bobToken.getType(),
//            carolSigningService.getAlgorithm(),
//            carolSigningService.getPublicKey(),
//            HashAlgorithm.SHA256
//        );
//        DirectAddress carolAddress = DirectAddress.create(carolRef);
//
//        // Bob transfers to Carol (no custom data)
//        TransferTransactionData carolTransferData = TransferTransactionData.create(
//            bobTokenState,
//            carolAddress,
//            randomBytes(32),
//            null, // Carol doesn't provide state
//            null,
//            List.of()
//        );
//
//        Commitment<TransferTransactionData> carolCommitment = client.submitTransaction(carolTransferData, bobSigningService).get();
//        InclusionProof carolProof = InclusionProofUtils.waitInclusionProof(client, carolCommitment).get();
//        Transaction<TransferTransactionData> carolTransferTx = client.createTransaction(carolCommitment, carolProof).get();
//
//        // Carol creates UnmaskedPredicate and finalizes
//        UnmaskedPredicate carolPredicate = UnmaskedPredicate.create(
//            bobToken.getId(),
//            bobToken.getType(),
//            carolSigningService,
//            HashAlgorithm.SHA256,
//            carolNonce
//        ).get();
//
//        TokenState carolTokenState = TokenState.create(carolPredicate, null);
//        Token carolToken = client.finishTransaction(
//            bobToken,
//            carolTokenState,
//            carolTransferTx
//        );
//
//        assertEquals(2, carolToken.getTransactions().size());
//    }
//
//    /**
//     * Test offline token transfer flow using commitments
//     */
//    public static void testOfflineTransferFlow(StateTransitionClient client) throws Exception {
//        // Mint initial token
//        byte[] aliceNonce = randomBytes(32);
//        SigningService aliceSigningService = SigningService.createFromSecret(ALICE_SECRET, aliceNonce);
//
//        TokenId tokenId = new TokenId(randomBytes(32));
//        TokenType tokenType = new TokenType(randomBytes(32));
//        TokenCoinData coinData = randomCoinData(2);
//
//        MaskedPredicate alicePredicate = MaskedPredicate.create(
//            tokenId,
//            tokenType,
//            aliceSigningService,
//            HashAlgorithm.SHA256,
//            aliceNonce
//        );
//
//        DirectAddress aliceAddress = alicePredicate.getReference().toAddress();
//        TokenState aliceTokenState = TokenState.create(alicePredicate, new byte[0]);
//
//        // Mint token
//        ISerializable tokenData = new ISerializable() {
//            @Override
//            public Object toJSON() {
//                return "{}";
//            }
//
//            @Override
//            public byte[] toCBOR() {
//                return new byte[0];
//            }
//        };
//
//        MintTransactionData mintData = MintTransactionData.create(
//            tokenId,
//            tokenType,
//            new byte[5],
//            coinData,
//            DirectAddress.create(new DataHash(HashAlgorithm.SHA256, new byte[32])),
//            randomBytes(5), // salt
//            null,
//            null
//        );
//
//        Commitment<MintTransactionData<?>> mintCommitment = client.submitMintTransaction(mintData).get();
//        InclusionProof mintProof = InclusionProofUtils.waitInclusionProof(client, mintCommitment).get();
//        Transaction<MintTransactionData<?>> mintTx = client.createTransaction(mintCommitment, mintProof).get();
//        Token token = new Token(aliceTokenState, mintTx);
//
//        // Bob prepares to receive
//        byte[] bobNonce = randomBytes(32);
//        SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce);
//        MaskedPredicate bobPredicate = MaskedPredicate.create(
//            token.getId(),
//            token.getType(),
//            bobSigningService,
//            HashAlgorithm.SHA256,
//            bobNonce
//        );
//        DirectAddress bobAddress = bobPredicate.getReference().toAddress();
//
//        // Create offline commitment
//        byte[] customData = "my custom data".getBytes(StandardCharsets.UTF_8);
//        TransferTransactionData transactionData = TransferTransactionData.create(
//            token.getState(),
//            bobAddress,
//            randomBytes(32),
//            new DataHasher(HashAlgorithm.SHA256).update(customData).digest(),
//            "my message".getBytes(StandardCharsets.UTF_8),
//            List.of()
//        );
//
//        Commitment<TransferTransactionData> commitment = client.submitTransaction(transactionData, aliceSigningService).get();
//
//        // Simulate offline transfer - Bob receives commitment and token
//        // In real world, commitment and token would be serialized to JSON and sent offline
//
//        // Bob waits for inclusion proof
//        InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(client, commitment).get();
//        Transaction<TransferTransactionData> confirmedTx = client.createTransaction(commitment, inclusionProof).get();
//
//        // Bob finalizes with his predicate
//        TokenState bobTokenState = TokenState.create(bobPredicate, customData);
//        Token bobToken = client.finishTransaction(
//            token,
//            bobTokenState,
//            confirmedTx
//        );
//
//        // Verify ownership transfer
//        assertEquals(token.getId(), bobToken.getId());
//        assertEquals(token.getType(), bobToken.getType());
//    }
    
    // Note: Token splitting functionality is not yet implemented in Java SDK
    // public static void testSplitFlow(StateTransitionClient client) throws Exception {
    //     // TODO: Implement when TokenSplitBuilder is available
    // }
}