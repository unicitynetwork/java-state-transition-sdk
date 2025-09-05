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
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.split.SplitMintReason;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferCommitment;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.transaction.split.TokenSplitBuilder;
import com.unicity.sdk.transaction.split.TokenSplitBuilder.TokenSplit;
import com.unicity.sdk.util.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;

/**
 * Common test flows for token operations, matching TypeScript SDK's CommonTestFlow.
 */
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

    MintCommitment<MintTransactionData<MintTransactionReason>> aliceMintCommitment = MintCommitment.create(
        new MintTransactionData<>(
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
    SubmitCommitmentResponse aliceMintTokenResponse = client
        .submitCommitment(aliceMintCommitment)
        .get();
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
    Token<?> aliceToken = new Token<>(
        aliceTokenState,
        aliceMintCommitment.toTransaction(mintInclusionProof)
    );

    assertTrue(aliceToken.verify().isSuccessful());

    String bobNameTag = UUID.randomUUID().toString();

    // Alice transfers to Bob
    String bobCustomData = "Bob's custom data";
    byte[] bobStateData = bobCustomData.getBytes(StandardCharsets.UTF_8);
    DataHash bobDataHash = new DataHasher(HashAlgorithm.SHA256).update(bobStateData).digest();

    // Submit transfer transaction
    TransferCommitment aliceToBobTransferCommitment = TransferCommitment.create(
        aliceToken,
        ProxyAddress.create(bobNameTag),
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
    Transaction<TransferTransactionData> aliceToBobTransferTransaction = aliceToBobTransferCommitment.toTransaction(
        aliceToken,
        aliceToBobTransferInclusionProof
    );

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
    MintCommitment<?> nametagMintCommitment = MintCommitment.create(
        MintTransactionData.createForNametag(
            bobNameTag,
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

    Transaction<? extends MintTransactionData<?>> bobNametagGenesis = nametagMintCommitment.toTransaction(
        InclusionProofUtils.waitInclusionProof(
            client,
            nametagMintCommitment
        ).get()
    );
    Token<?> bobNametagToken = new Token<>(
        new NameTagTokenState(bobNametagPredicate, bobAddress),
        bobNametagGenesis
    );

    // Bob finalizes the token
    Token<?> bobToken = client.finalizeTransaction(
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
    TransferCommitment bobToCarolTransferCommitment = TransferCommitment.create(
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
    Transaction<TransferTransactionData> bobToCarolTransaction = bobToCarolTransferCommitment.toTransaction(
        bobToken,
        bobToCarolInclusionProof
    );

    // Carol creates UnmaskedPredicate and finalizes
    UnmaskedPredicate carolPredicate = UnmaskedPredicate.create(
        carolSigningService,
        HashAlgorithm.SHA256,
        carolNonce
    );

    Token<?> carolToken = client.finalizeTransaction(
        bobToken,
        new TokenState(carolPredicate, null),
        bobToCarolTransaction
    );

    assertTrue(carolToken.verify().isSuccessful());
    assertEquals(2, carolToken.getTransactions().size());

    // Bob receives carol token with nametag
    byte[] carolToBobNonce = randomBytes(32);
    UnmaskedPredicate carolToBobPredicate = UnmaskedPredicate.create(
        SigningService.createFromSecret(BOB_SECRET, carolToBobNonce),
        HashAlgorithm.SHA256,
        carolToBobNonce
    );

    byte[] bobNametagSecondUseNonce = randomBytes(32);
    NameTagTokenState nametagSecondUseTokenState = new NameTagTokenState(
        MaskedPredicate.create(
            SigningService.createFromSecret(BOB_SECRET, bobNametagSecondUseNonce),
            HashAlgorithm.SHA256,
            bobNametagSecondUseNonce
        ),
        carolToBobPredicate.getReference(carolToken.getType()).toAddress()
    );

    TransferCommitment nametagSecondUseCommitment = TransferCommitment.create(
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

    Token<?> bobSecondUseNametag = client.finalizeTransaction(
        bobNametagToken,
        nametagSecondUseTokenState,
        nametagSecondUseCommitment.toTransaction(bobNametagToken,
            InclusionProofUtils.waitInclusionProof(client, nametagSecondUseCommitment).get())
    );

    TransferCommitment carolToBobTransferCommitment = TransferCommitment.create(
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

    Transaction<TransferTransactionData> carolToBobTransaction = carolToBobTransferCommitment.toTransaction(
        carolToken,
        carolToBobInclusionProof
    );

    Token<?> carolToBobToken = client.finalizeTransaction(
        carolToken,
        new TokenState(carolToBobPredicate, null),
        carolToBobTransaction,
        List.of(bobSecondUseNametag)
    );

    assertTrue(carolToBobToken.verify().isSuccessful());

    // SPLIT
    Entry<CoinId, BigInteger>[] splitCoins = coinData.getCoins().entrySet()
        .toArray(Map.Entry[]::new);

    TokenType splitTokenType = new TokenType(randomBytes(32));
    byte[] splitTokenNonce = randomBytes(32);
    MaskedPredicate splitTokenPredicate = MaskedPredicate.create(
        SigningService.createFromSecret(BOB_SECRET, splitTokenNonce),
        HashAlgorithm.SHA256,
        splitTokenNonce
    );

    TokenSplit split = new TokenSplitBuilder()
        .createToken(
            new TokenId(randomBytes(32)),
            splitTokenType,
            null,
            new TokenCoinData(Map.ofEntries(splitCoins[0])),
            splitTokenPredicate.getReference(splitTokenType).toAddress(),
            randomBytes(32),
            null
        )
        .createToken(
            new TokenId(randomBytes(32)),
            splitTokenType,
            null,
            new TokenCoinData(Map.ofEntries(splitCoins[1])),
            splitTokenPredicate.getReference(splitTokenType).toAddress(),
            randomBytes(32),
            null
        )
        .build(carolToBobToken);

    TransferCommitment burnCommitment = split.createBurnCommitment(randomBytes(32),
        SigningService.createFromSecret(BOB_SECRET, carolToBobNonce));

    if (client.submitCommitment(carolToBobToken, burnCommitment).get().getStatus()
        != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception("Failed to submit burn commitment");
    }

    List<MintCommitment<MintTransactionData<SplitMintReason>>> splitCommitments = split.createSplitMintCommitments(
        burnCommitment.toTransaction(carolToBobToken, InclusionProofUtils.waitInclusionProof(
            client,
            burnCommitment
        ).get())
    );

    List<Transaction<MintTransactionData<SplitMintReason>>> splitTransactions = new ArrayList<>();
    for (MintCommitment<MintTransactionData<SplitMintReason>> commitment : splitCommitments) {
      if (client.submitCommitment(commitment).get().getStatus() != SubmitCommitmentStatus.SUCCESS) {
        throw new Exception("Failed to submit split mint commitment");
      }

      splitTransactions.add(commitment.toTransaction(
          InclusionProofUtils.waitInclusionProof(client, commitment).get()));
    }
    Assertions.assertEquals(
        2,
        splitTransactions.stream()
            .map(transaction -> transaction.getData().getReason().get().verify(transaction)
                .isSuccessful())
            .filter(Boolean::booleanValue)
            .count()
    );

    Assertions.assertTrue(
        new Token<>(
            new TokenState(splitTokenPredicate, null),
            splitTransactions.get(0)
        ).verify().isSuccessful()
    );


  }
}