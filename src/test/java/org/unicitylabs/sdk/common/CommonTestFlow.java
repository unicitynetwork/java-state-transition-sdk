package org.unicitylabs.sdk.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.CertificationResponse;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicateReference;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.DefaultMintReasonFactory;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintReasonFactory;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransaction;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder.TokenSplit;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TokenUtils;

/**
 * Common test flows for token operations, matching TypeScript SDK's CommonTestFlow.
 */
public abstract class CommonTestFlow {

  protected StateTransitionClient client;
  protected RootTrustBase trustBase;

  private final MintReasonFactory mintReasonFactory = new DefaultMintReasonFactory();

  private static final byte[] ALICE_SECRET = "Alice".getBytes(StandardCharsets.UTF_8);
  private static final byte[] BOB_SECRET = "Bob".getBytes(StandardCharsets.UTF_8);
  private static final byte[] CAROL_SECRET = "Carol".getBytes(StandardCharsets.UTF_8);

  /**
   * Test basic token transfer flow: Alice -> Bob -> Carol
   */
  @Test
  public void testTransferFlow() throws Exception {
    Token aliceToken = TokenUtils.mintToken(
        this.client,
        this.trustBase,
        this.mintReasonFactory,
        ALICE_SECRET
    );

    assertTrue(this.client.isMinted(aliceToken.getId(), this.trustBase).get());
    assertTrue(aliceToken.verify(this.trustBase, this.mintReasonFactory).isSuccessful());

    String bobNameTag = UUID.randomUUID().toString();

    // Alice transfers to Bob
    String bobCustomData = "Bob's custom data";
    byte[] bobStateData = bobCustomData.getBytes(StandardCharsets.UTF_8);
    DataHash bobDataHash = new DataHasher(HashAlgorithm.SHA256).update(bobStateData).digest();

    // Submit transfer transaction
    SigningService aliceSigningService = SigningService.createFromMaskedSecret(
        ALICE_SECRET,
        ((MaskedPredicate) aliceToken.getState().getPredicate()).getNonce()
    );
    TransferCommitment aliceToBobTransferCommitment = TransferCommitment.create(
        aliceToken,
        ProxyAddress.create(bobNameTag),
        randomBytes(32),
        bobDataHash,
        null,
        aliceSigningService
    );
    CertificationResponse aliceToBobTransferSubmitResponse = this.client.submitCommitment(
        aliceToBobTransferCommitment
    ).get();

    if (aliceToBobTransferSubmitResponse.getStatus() != CertificationStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          aliceToBobTransferSubmitResponse.getStatus()));
    }

    // Wait for inclusion proof
    InclusionProof aliceToBobTransferInclusionProof = InclusionProofUtils.waitInclusionProof(
        this.client,
        this.trustBase,
        aliceToBobTransferCommitment
    ).get();

    // Create transfer transaction
    TransferTransaction aliceToBobTransferTransaction = aliceToBobTransferCommitment.toTransaction(
        aliceToBobTransferInclusionProof
    );

    // Check if alice token is spent
    assertTrue(this.client.isStateSpent(aliceToken, aliceSigningService.getPublicKey(), this.trustBase).get());

    // Bob prepares to receive the token
    DirectAddress bobAddress = UnmaskedPredicateReference.create(
        aliceToken.getType(),
        SigningService.createFromSecret(BOB_SECRET),
        HashAlgorithm.SHA256
    ).toAddress();

    // Bob mints a name tag tokens
    Token bobNametagToken = TokenUtils.mintNametagToken(
        this.client,
        this.trustBase,
        this.mintReasonFactory,
        BOB_SECRET,
        bobNameTag,
        bobAddress
    );

    // Bob finalizes the token
    Token bobToken = client.finalizeTransaction(
        this.trustBase,
        this.mintReasonFactory,
        aliceToken,
        new TokenState(
            UnmaskedPredicate.create(
                aliceToken.getId(),
                aliceToken.getType(),
                aliceToBobTransferTransaction,
                SigningService.createFromSecret(BOB_SECRET),
                HashAlgorithm.SHA256
            ),
            bobStateData
        ),
        aliceToBobTransferTransaction,
        List.of(bobNametagToken)
    );

    // Verify Bob is now the owner
    assertTrue(bobToken.verify(this.trustBase, this.mintReasonFactory).isSuccessful());
    assertTrue(PredicateEngineService
        .createPredicate(bobToken.getState().getPredicate())
        .isOwner(SigningService.createFromSecret(BOB_SECRET).getPublicKey())
    );
    assertEquals(aliceToken.getId(), bobToken.getId());
    assertEquals(aliceToken.getType(), bobToken.getType());

    // Transfer to Carol with UnmaskedPredicate
    DirectAddress carolAddress = UnmaskedPredicateReference.create(
        bobToken.getType(),
        SigningService.createFromSecret(CAROL_SECRET),
        HashAlgorithm.SHA256).toAddress();

    // Bob transfers to Carol (no custom data)
    // Submit transfer transaction
    TransferCommitment bobToCarolTransferCommitment = TransferCommitment.create(
        bobToken,
        carolAddress,
        randomBytes(32),
        null,
        null,
        SigningService.createFromSecret(BOB_SECRET)
    );
    CertificationResponse bobToCarolTransferSubmitResponse = client.submitCommitment(
        bobToCarolTransferCommitment
    ).get();

    if (bobToCarolTransferSubmitResponse.getStatus() != CertificationStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          bobToCarolTransferSubmitResponse.getStatus()));
    }

    InclusionProof bobToCarolInclusionProof = InclusionProofUtils.waitInclusionProof(
        this.client,
        this.trustBase,
        bobToCarolTransferCommitment
    ).get();
    TransferTransaction bobToCarolTransaction = bobToCarolTransferCommitment.toTransaction(
        bobToCarolInclusionProof
    );

    // Carol creates UnmaskedPredicate and finalizes
    UnmaskedPredicate carolPredicate = UnmaskedPredicate.create(
        bobToken.getId(),
        bobToken.getType(),
        bobToCarolTransaction,
        SigningService.createFromSecret(CAROL_SECRET),
        HashAlgorithm.SHA256
    );

    Token carolToken = this.client.finalizeTransaction(
        this.trustBase,
        this.mintReasonFactory,
        bobToken,
        new TokenState(carolPredicate, null),
        bobToCarolTransaction
    );

    assertTrue(carolToken.verify(this.trustBase, this.mintReasonFactory).isSuccessful());
    assertEquals(2, carolToken.getTransactions().size());

    // Bob receives carol token with nametag
    TransferCommitment carolToBobTransferCommitment = TransferCommitment.create(
        carolToken,
        ProxyAddress.create(bobNameTag),
        randomBytes(32),
        null,
        null,
        SigningService.createFromSecret(CAROL_SECRET)
    );
    CertificationResponse carolToBobTransferSubmitResponse = this.client.submitCommitment(
        carolToBobTransferCommitment
    ).get();

    if (carolToBobTransferSubmitResponse.getStatus() != CertificationStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          carolToBobTransferSubmitResponse.getStatus()));
    }

    InclusionProof carolToBobInclusionProof = InclusionProofUtils.waitInclusionProof(
        this.client,
        this.trustBase,
        carolToBobTransferCommitment
    ).get();

    TransferTransaction carolToBobTransaction = carolToBobTransferCommitment.toTransaction(
        carolToBobInclusionProof
    );

    Token carolToBobToken = client.finalizeTransaction(
        this.trustBase,
        this.mintReasonFactory,
        carolToken,
        new TokenState(
            UnmaskedPredicate.create(
                carolToken.getId(),
                carolToken.getType(),
                carolToBobTransaction,
                SigningService.createFromSecret(BOB_SECRET),
                HashAlgorithm.SHA256
            ),
            null
        ),
        carolToBobTransaction,
        List.of(bobNametagToken)
    );

    assertTrue(carolToBobToken.verify(this.trustBase, this.mintReasonFactory).isSuccessful());

    // SPLIT
    List<Map.Entry<CoinId, BigInteger>> splitCoins = carolToken.getCoins()
        .map(data -> List.copyOf(data.getCoins().entrySet()))
        .orElse(List.of());

    TokenType splitTokenType = new TokenType(randomBytes(32));
    byte[] splitTokenNonce = randomBytes(32);

    TokenSplit split = new TokenSplitBuilder()
        .createToken(
            new TokenId(randomBytes(32)),
            splitTokenType,
            null,
            new TokenCoinData(Map.ofEntries(splitCoins.get(0))),
            MaskedPredicateReference.create(
                splitTokenType,
                SigningService.createFromMaskedSecret(BOB_SECRET, splitTokenNonce),
                HashAlgorithm.SHA256,
                splitTokenNonce
            ).toAddress(),
            randomBytes(32),
            null
        )
        .createToken(
            new TokenId(randomBytes(32)),
            splitTokenType,
            null,
            new TokenCoinData(Map.ofEntries(splitCoins.get(1))),
            MaskedPredicateReference.create(
                splitTokenType,
                SigningService.createFromMaskedSecret(BOB_SECRET, splitTokenNonce),
                HashAlgorithm.SHA256,
                splitTokenNonce
            ).toAddress(),
            randomBytes(32),
            null
        )
        .build(carolToBobToken);

    TransferCommitment burnCommitment = split.createBurnCommitment(
        randomBytes(32),
        SigningService.createFromSecret(BOB_SECRET)
    );

    if (client.submitCommitment(burnCommitment).get().getStatus()
        != CertificationStatus.SUCCESS) {
      throw new Exception("Failed to submit burn commitment");
    }

    List<MintCommitment> splitCommitments = split.createSplitMintCommitments(
        this.trustBase,
        this.mintReasonFactory,
        burnCommitment.toTransaction(
            InclusionProofUtils.waitInclusionProof(
                this.client,
                this.trustBase,
                burnCommitment
            ).get()
        )
    );

    List<MintTransaction> splitTransactions = new ArrayList<>();
    for (MintCommitment commitment : splitCommitments) {
      if (client.submitCommitment(commitment).get().getStatus() != CertificationStatus.SUCCESS) {
        throw new Exception("Failed to submit split mint commitment");
      }

      splitTransactions.add(commitment.toTransaction(
          InclusionProofUtils.waitInclusionProof(this.client, this.trustBase, commitment).get()));
    }
    Assertions.assertEquals(
        2,
        splitTransactions.stream()
            .map(transaction -> transaction.verify(this.trustBase, this.mintReasonFactory).isSuccessful())
            .filter(Boolean::booleanValue)
            .count()
    );

    MaskedPredicate splitTokenPredicate = MaskedPredicate.create(
        splitTransactions.get(0).getData().getTokenId(),
        splitTransactions.get(0).getData().getTokenType(),
        SigningService.createFromMaskedSecret(BOB_SECRET, splitTokenNonce),
        HashAlgorithm.SHA256,
        splitTokenNonce
    );

    Assertions.assertDoesNotThrow(() ->
        Token.mint(
            this.trustBase,
            this.mintReasonFactory,
            new TokenState(splitTokenPredicate, null),
            splitTransactions.get(0)
        )
    );
  }
}