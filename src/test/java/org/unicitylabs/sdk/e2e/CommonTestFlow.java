package org.unicitylabs.sdk.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;
import static org.unicitylabs.sdk.utils.TestUtils.randomCoinData;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
import org.unicitylabs.sdk.predicate.UnmaskedPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.transaction.NametagMintTransactionData;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder.TokenSplit;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TestTokenData;

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
    DirectAddress bobAddress = UnmaskedPredicateReference.create(
        tokenType,
        SigningService.createFromSecret(BOB_SECRET, null),
        HashAlgorithm.SHA256
    ).toAddress();

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
        new NametagMintTransactionData<>(
            bobNameTag,
            bobNametagTokenType,
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
        new TokenState(bobNametagPredicate, null),
        bobNametagGenesis
    );

    // Bob finalizes the token
    Token<?> bobToken = client.finalizeTransaction(
        aliceToken,
        new TokenState(
            UnmaskedPredicate.create(
                SigningService.createFromSecret(BOB_SECRET, null),
                HashAlgorithm.SHA256,
                aliceToBobTransferTransaction.getData().getSalt()
            ),
            bobStateData
        ),
        aliceToBobTransferTransaction,
        List.of(bobNametagToken)
    );

    // Verify Bob is now the owner
    assertTrue(bobToken.verify().isSuccessful());
    assertTrue(bobToken.getState().getUnlockPredicate()
        .isOwner(SigningService.createFromSecret(BOB_SECRET, null).getPublicKey())
    );
    assertEquals(aliceToken.getId(), bobToken.getId());
    assertEquals(aliceToken.getType(), bobToken.getType());

    // Transfer to Carol with UnmaskedPredicate
    DirectAddress carolAddress = UnmaskedPredicateReference.create(
        tokenType,
        SigningService.createFromSecret(CAROL_SECRET, null),
        HashAlgorithm.SHA256).toAddress();

    // Bob transfers to Carol (no custom data)
    // Submit transfer transaction
    TransferCommitment bobToCarolTransferCommitment = TransferCommitment.create(
        bobToken,
        carolAddress,
        randomBytes(32),
        null,
        null,
        SigningService.createFromSecret(BOB_SECRET, null)
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
        SigningService.createFromSecret(CAROL_SECRET, null),
        HashAlgorithm.SHA256,
        bobToCarolTransaction.getData().getSalt()
    );

    Token<?> carolToken = client.finalizeTransaction(
        bobToken,
        new TokenState(carolPredicate, null),
        bobToCarolTransaction
    );

    assertTrue(carolToken.verify().isSuccessful());
    assertEquals(2, carolToken.getTransactions().size());

    // Bob receives carol token with nametag
    TransferCommitment carolToBobTransferCommitment = TransferCommitment.create(
        carolToken,
        ProxyAddress.create(bobNameTag),
        randomBytes(32),
        null,
        null,
        SigningService.createFromSecret(CAROL_SECRET, null)
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
        new TokenState(
            UnmaskedPredicate.create(
                SigningService.createFromSecret(BOB_SECRET, null),
                HashAlgorithm.SHA256,
                carolToBobTransaction.getData().getSalt()
            ),
            null
        ),
        carolToBobTransaction,
        List.of(bobNametagToken)
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

    TransferCommitment burnCommitment = split.createBurnCommitment(
        randomBytes(32),
        SigningService.createFromSecret(BOB_SECRET, null)
    );

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