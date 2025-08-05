package com.unicity.sdk.e2e;

import static com.unicity.sdk.utils.TestUtils.randomBytes;
import static com.unicity.sdk.utils.TestUtils.randomCoinData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.DirectAddress;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicate;
import com.unicity.sdk.predicate.UnmaskedPredicateReference;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.NameTagTokenState;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.NameTagMintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.utils.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

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
    TokenId tokenId = new TokenId(randomBytes(32));
    TokenType tokenType = new TokenType(randomBytes(32));
    TokenCoinData coinData = randomCoinData(2);

    // Alice mints a token
    byte[] aliceNonce = randomBytes(32);
    SigningService aliceSigningService = SigningService.createFromSecret(ALICE_SECRET, aliceNonce);

    MaskedPredicate alicePredicate = MaskedPredicate.create(
        aliceSigningService,
        HashAlgorithm.SHA256,
        aliceNonce
    );

    Address aliceAddress = alicePredicate.getReference(tokenType).toAddress();
    TokenState aliceTokenState = new TokenState(alicePredicate, null);

    Commitment<MintTransactionData<?>> aliceMintCommitment = Commitment.create(
        new MintTransactionData(
            tokenId,
            tokenType,
            new TestTokenData(randomBytes(32)).getData(),
            coinData,
            aliceAddress,
            new byte[5],
            null,
            null
        ));

    // Submit mint transaction using StateTransitionClient
    SubmitCommitmentResponse aliceMintTokenResponse = client.submitCommitment(
        aliceMintCommitment).get();
    if (aliceMintTokenResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit mint commitment: %s",
          aliceMintTokenResponse.getStatus()));
    }

    // Wait for inclusion proof
    InclusionProof mintInclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        aliceMintCommitment
    ).get();

    // Create mint transaction
    Token aliceToken = new Token(
        aliceTokenState,
        client.createTransaction(aliceMintCommitment, mintInclusionProof)
    );

    // Bob prepares to receive the token
    byte[] bobNonce = randomBytes(32);
    SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce);
    MaskedPredicate bobPredicate = MaskedPredicate.create(bobSigningService, HashAlgorithm.SHA256,
        bobNonce);
    DirectAddress bobAddress = bobPredicate.getReference(tokenType).toAddress();


    // Bob mints a name tag tokens
    MaskedPredicate bobNametagPredicate = MaskedPredicate.create(
        bobSigningService,
        HashAlgorithm.SHA256,
        randomBytes(32)
    );
    TokenType bobNametagTokenType = new TokenType(randomBytes(32));
    DirectAddress bobNametagAddress = bobNametagPredicate.getReference(tokenType).toAddress();
    NameTagTokenState bobNametagTokenState = new NameTagTokenState(bobNametagPredicate,
        bobAddress);
    Commitment<NameTagMintTransactionData> nametagMintCommitment = Commitment.create(
        NameTagMintTransactionData.create(
            UUID.randomUUID().toString(),
            bobNametagTokenType,
            new byte[10],
            null,
            bobNametagAddress,
            randomBytes(32),
            bobNametagTokenState
        ));
    SubmitCommitmentResponse nametagMintResponse = client.submitCommitment(nametagMintCommitment)
        .get();
    if (nametagMintResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit nametag mint commitment: %s",
          nametagMintResponse.getStatus()));
    }

    Transaction<NameTagMintTransactionData> bobNametagGenesis = client.createTransaction(
        nametagMintCommitment,
        InclusionProofUtils.waitInclusionProof(
            client,
            nametagMintCommitment
        ).get()
    );
    Token<?> bobNametagToken = new Token(bobNametagTokenState, bobNametagGenesis);

    // Alice transfers to Bob
    String bobCustomData = "Bob's custom data";
    byte[] bobStateData = bobCustomData.getBytes(StandardCharsets.UTF_8);
    DataHash bobDataHash = new DataHasher(HashAlgorithm.SHA256).update(bobStateData).digest();

    TransferTransactionData aliceToBobTransferData = new TransferTransactionData(
        aliceTokenState,
        ProxyAddress.create(bobNametagGenesis.getData().getTokenId()),
        randomBytes(32),
        bobDataHash,
        null,
        List.of()
    );

    // Submit transfer transaction
    Commitment<TransferTransactionData> aliceToBobTransferCommitment = Commitment.create(
        aliceToken,
        aliceToBobTransferData,
        aliceSigningService
    );
    SubmitCommitmentResponse aliceToBobTransferSubmitResponse = client.submitCommitment(aliceToken,
        aliceToBobTransferCommitment).get();

    if (aliceToBobTransferSubmitResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          aliceToBobTransferSubmitResponse.getStatus()));
    }

    // Wait for inclusion proof
    InclusionProof aliceToBobTransferInclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        aliceToBobTransferCommitment
    ).get();

    // Create transfer transaction
    Transaction<TransferTransactionData> aliceToBobTransferTransaction = client.createTransaction(
        aliceToken,
        aliceToBobTransferCommitment,
        aliceToBobTransferInclusionProof
    );

    // Bob finalizes the token
    TokenState bobTokenState = new TokenState(bobPredicate, bobStateData);
    Token bobToken = client.finishTransaction(
        aliceToken,
        bobTokenState,
        aliceToBobTransferTransaction,
        List.of(bobNametagToken)
    );

    // Verify Bob is now the owner
    assertTrue(bobToken.getState().getUnlockPredicate().isOwner(bobSigningService.getPublicKey()));
    assertEquals(aliceToken.getId(), bobToken.getId());
    assertEquals(aliceToken.getType(), bobToken.getType());

    // Transfer to Carol with UnmaskedPredicate
    byte[] carolNonce = randomBytes(32);
    SigningService carolSigningService = SigningService.createFromSecret(CAROL_SECRET, carolNonce);
    DirectAddress carolAddress = UnmaskedPredicateReference.create(tokenType, carolSigningService,
        HashAlgorithm.SHA256).toAddress();

    // Bob transfers to Carol (no custom data)
    TransferTransactionData bobToCarolTransferData = new TransferTransactionData(
        bobTokenState,
        carolAddress,
        randomBytes(32),
        null, // Carol doesn't provide state
        null,
        List.of()
    );

    // Submit transfer transaction
    Commitment<TransferTransactionData> bobToCarolTransferCommitment = Commitment.create(
        bobToken,
        bobToCarolTransferData,
        bobSigningService
    );
    SubmitCommitmentResponse bobToCarolTransferSubmitResponse = client.submitCommitment(
        bobToken,
        bobToCarolTransferCommitment
    ).get();

    if (bobToCarolTransferSubmitResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          aliceToBobTransferSubmitResponse.getStatus()));
    }

    InclusionProof bobToCarolInclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        bobToCarolTransferCommitment
    ).get();
    Transaction<TransferTransactionData> bobToCarolTransaction = client.createTransaction(
        bobToken,
        bobToCarolTransferCommitment,
        bobToCarolInclusionProof
    );

    // Carol creates UnmaskedPredicate and finalizes
    UnmaskedPredicate carolPredicate = UnmaskedPredicate.create(
        carolSigningService,
        HashAlgorithm.SHA256,
        carolNonce
    );

    Token carolToken = client.finishTransaction(
        bobToken,
        new TokenState(carolPredicate, null),
        bobToCarolTransaction
    );

    assertEquals(2, carolToken.getTransactions().size());
  }
}