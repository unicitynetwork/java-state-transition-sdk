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

    assertTrue(aliceToken.verify().isSuccessful());

    // Bob prepares to receive the token
    byte[] bobNonce = randomBytes(32);
    SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce);
    MaskedPredicate bobPredicate = MaskedPredicate.create(bobSigningService, HashAlgorithm.SHA256,
        bobNonce);
    DirectAddress bobAddress = bobPredicate.getReference(tokenType).toAddress();

    // Bob mints a name tag tokens
    byte[] bobNametagNonce = randomBytes(32);
    MaskedPredicate bobNametagPredicate = MaskedPredicate.create(
        SigningService.createFromSecret(BOB_SECRET, bobNametagNonce),
        HashAlgorithm.SHA256,
        bobNametagNonce
    );
    TokenType bobNametagTokenType = new TokenType(randomBytes(32));
    DirectAddress bobNametagAddress = bobNametagPredicate.getReference(bobNametagTokenType)
        .toAddress();
    Commitment<MintTransactionData<?>> nametagMintCommitment = Commitment.create(
        MintTransactionData.createNametag(
            UUID.randomUUID().toString(),
            bobNametagTokenType,
            new byte[10],
            null,
            bobNametagAddress,
            randomBytes(32),
            bobAddress
        ));
    SubmitCommitmentResponse nametagMintResponse = client.submitCommitment(nametagMintCommitment)
        .get();
    if (nametagMintResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit nametag mint commitment: %s",
          nametagMintResponse.getStatus()));
    }

    Transaction<MintTransactionData<?>> bobNametagGenesis = client.createTransaction(
        nametagMintCommitment,
        InclusionProofUtils.waitInclusionProof(
            client,
            nametagMintCommitment
        ).get()
    );
    Token<?> bobNametagToken = new Token(
        new NameTagTokenState(bobNametagPredicate, bobAddress),
        bobNametagGenesis
    );

    // Alice transfers to Bob
    String bobCustomData = "Bob's custom data";
    byte[] bobStateData = bobCustomData.getBytes(StandardCharsets.UTF_8);
    DataHash bobDataHash = new DataHasher(HashAlgorithm.SHA256).update(bobStateData).digest();

    // Submit transfer transaction
    Commitment<TransferTransactionData> aliceToBobTransferCommitment = Commitment.create(
        aliceToken,
        ProxyAddress.create(bobNametagGenesis.getData().getTokenId()),
        randomBytes(32),
        bobDataHash,
        null,
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
    Token bobToken = client.finishTransaction(
        aliceToken,
        new TokenState(bobPredicate, bobStateData),
        aliceToBobTransferTransaction,
        List.of(bobNametagToken)
    );

    // Verify Bob is now the owner
    assertTrue(bobToken.verify().isSuccessful());
    assertTrue(bobToken.getState().getUnlockPredicate().isOwner(bobSigningService.getPublicKey()));
    assertEquals(aliceToken.getId(), bobToken.getId());
    assertEquals(aliceToken.getType(), bobToken.getType());

    // Transfer to Carol with UnmaskedPredicate
    byte[] carolNonce = randomBytes(32);
    SigningService carolSigningService = SigningService.createFromSecret(CAROL_SECRET, carolNonce);
    DirectAddress carolAddress = UnmaskedPredicateReference.create(tokenType, carolSigningService,
        HashAlgorithm.SHA256).toAddress();

    // Bob transfers to Carol (no custom data)
    // Submit transfer transaction
    Commitment<TransferTransactionData> bobToCarolTransferCommitment = Commitment.create(
        bobToken,
        carolAddress,
        randomBytes(32),
        null,
        null,
        bobSigningService
    );
    SubmitCommitmentResponse bobToCarolTransferSubmitResponse = client.submitCommitment(
        bobToken,
        bobToCarolTransferCommitment
    ).get();

    if (bobToCarolTransferSubmitResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          bobToCarolTransferSubmitResponse.getStatus()));
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

    assertTrue(carolToken.verify().isSuccessful());
    assertEquals(2, carolToken.getTransactions().size());

    // Bob receives carol token with nametag
    byte[] bobReceivesTokenFromCarolNonce = randomBytes(32);
    UnmaskedPredicate bobReceivesTokenFromCarolPredicate = UnmaskedPredicate.create(
        SigningService.createFromSecret(BOB_SECRET, bobReceivesTokenFromCarolNonce),
        HashAlgorithm.SHA256,
        bobReceivesTokenFromCarolNonce
    );

    byte[] bobNametagSecondUseNonce = randomBytes(32);
    NameTagTokenState nametagSecondUseTokenState = new NameTagTokenState(
        MaskedPredicate.create(
            SigningService.createFromSecret(BOB_SECRET, bobNametagSecondUseNonce),
            HashAlgorithm.SHA256,
            bobNametagSecondUseNonce
        ),
        bobReceivesTokenFromCarolPredicate.getReference(carolToken.getType()).toAddress()
    );

    Commitment<TransferTransactionData> nametagSecondUseCommitment = Commitment.create(
        bobNametagToken,
        nametagSecondUseTokenState.getUnlockPredicate().getReference(bobNametagToken.getType())
            .toAddress(),
        randomBytes(32),
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                nametagSecondUseTokenState.getData()
                    .orElseThrow(
                        () -> new RuntimeException("Invalid nametag, address data missing")
                    )
            )
            .digest(),
        null,
        SigningService.createFromSecret(BOB_SECRET, bobNametagNonce)
    );
    SubmitCommitmentResponse nametagSecondUseResponse = client.submitCommitment(
        bobNametagToken,
        nametagSecondUseCommitment
    ).get();

    if (nametagSecondUseResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit nametag transfer commitment: %s",
          nametagMintResponse.getStatus()));
    }

    Token<?> bobSecondUseNametag = client.finishTransaction(
        bobNametagToken,
        nametagSecondUseTokenState,
        client.createTransaction(bobNametagToken, nametagSecondUseCommitment,
            InclusionProofUtils.waitInclusionProof(client, nametagSecondUseCommitment).get())
    );

    Commitment<TransferTransactionData> carolToBobTransferCommitment = Commitment.create(
        carolToken,
        ProxyAddress.create(bobNametagToken.getId()),
        randomBytes(32),
        null,
        null,
        carolSigningService
    );
    SubmitCommitmentResponse carolToBobTransferSubmitResponse = client.submitCommitment(
        carolToken,
        carolToBobTransferCommitment
    ).get();

    if (carolToBobTransferSubmitResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          carolToBobTransferSubmitResponse.getStatus()));
    }

    InclusionProof carolToBobInclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        carolToBobTransferCommitment
    ).get();

    Transaction<TransferTransactionData> carolToBobTransaction = client.createTransaction(
        carolToken,
        carolToBobTransferCommitment,
        carolToBobInclusionProof
    );

    assertTrue(client.finishTransaction(
        carolToken,
        new TokenState(bobReceivesTokenFromCarolPredicate, null),
        carolToBobTransaction,
        List.of(bobSecondUseNametag)
    ).verify().isSuccessful());

  }
}