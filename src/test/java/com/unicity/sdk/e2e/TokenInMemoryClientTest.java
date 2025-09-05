package com.unicity.sdk.e2e;

import static com.unicity.sdk.utils.TestUtils.randomBytes;
import static com.unicity.sdk.utils.TestUtils.randomCoinData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.TestAggregatorClient;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.NameTagTokenState;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.NametagMintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferCommitment;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.util.InclusionProofUtils;
import com.unicity.sdk.utils.TestTokenData;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests for token operations using CommonTestFlow. Matches TypeScript SDK's test
 * structure.
 */
public class TokenInMemoryClientTest {

  public static Token<?> mintToken(StateTransitionClient client, byte[] secret) throws Exception {
    TokenId tokenId = new TokenId(randomBytes(32));
    TokenType tokenType = new TokenType(randomBytes(32));
    TokenCoinData coinData = randomCoinData(2);
    byte[] nonce = randomBytes(32);
    SigningService signingService = SigningService.createFromSecret(secret, nonce);

    MaskedPredicate predicate = MaskedPredicate.create(
        signingService,
        HashAlgorithm.SHA256,
        nonce
    );

    Address address = predicate.getReference(tokenType).toAddress();
    TokenState tokenState = new TokenState(predicate, null);

    MintCommitment<MintTransactionData<MintTransactionReason>> commitment = MintCommitment.create(
        new MintTransactionData<>(
            tokenId,
            tokenType,
            new TestTokenData(randomBytes(32)).getData(),
            coinData,
            address,
            new byte[5],
            null,
            null
        )
    );

    // Submit mint transaction using StateTransitionClient
    SubmitCommitmentResponse response = client
        .submitCommitment(commitment)
        .get();
    if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit mint commitment: %s",
          response.getStatus()));
    }

    // Wait for inclusion proof
    InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        commitment
    ).get();

    // Create mint transaction
    return new Token<>(
        tokenState,
        commitment.toTransaction(inclusionProof)
    );
  }

  public static Token<?> mintNametagToken(StateTransitionClient client, byte[] secret,
      String nametag, Address targetAddress) throws Exception {
    TokenType tokenType = new TokenType(randomBytes(32));
    TokenCoinData coinData = randomCoinData(2);
    byte[] nonce = randomBytes(32);
    SigningService signingService = SigningService.createFromSecret(secret, nonce);

    MaskedPredicate predicate = MaskedPredicate.create(
        signingService,
        HashAlgorithm.SHA256,
        nonce
    );

    Address address = predicate.getReference(tokenType).toAddress();
    TokenState tokenState = new NameTagTokenState(predicate, targetAddress);

    MintCommitment<NametagMintTransactionData<MintTransactionReason>> commitment = MintCommitment.create(
        new NametagMintTransactionData<>(
            nametag,
            tokenType,
            new TestTokenData(randomBytes(32)).getData(),
            coinData,
            address,
            new byte[5],
            targetAddress
        )
    );

    // Submit mint transaction using StateTransitionClient
    SubmitCommitmentResponse response = client
        .submitCommitment(commitment)
        .get();
    if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit mint commitment: %s",
          response.getStatus()));
    }

    // Wait for inclusion proof
    InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        commitment
    ).get();

    // Create mint transaction
    return new Token<>(
        tokenState,
        commitment.toTransaction(inclusionProof)
    );
  }

  public static Transaction<TransferTransactionData> transferToken(
      StateTransitionClient client,
      byte[] secret,
      Token<?> token,
      Address targetAddress,
      DataHash dataHash
  ) throws Exception {
    TransferCommitment commitment = TransferCommitment.create(
        token,
        targetAddress,
        randomBytes(32),
        dataHash,
        null,
        SigningService.createFromSecret(secret, token.getState().getUnlockPredicate().getNonce())
    );

    SubmitCommitmentResponse response = client.submitCommitment(token, commitment).get();

    if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit transaction commitment: %s",
          response.getStatus()));
    }

    // Wait for inclusion proof
    InclusionProof inclusionProof = InclusionProofUtils.waitInclusionProof(
        client,
        commitment
    ).get();

    // Create transfer transaction
    return commitment.toTransaction(
        token,
        inclusionProof
    );
  }

  @Test
  public void testNametagTransfer() throws Exception {
    StateTransitionClient client = new StateTransitionClient(
        new TestAggregatorClient());
    byte[] ALICE_SECRET = "ALICE".getBytes(StandardCharsets.UTF_8);
    byte[] BOB_SECRET = "BOB".getBytes(StandardCharsets.UTF_8);

    // Alice mints a token
    Token<?> aliceToken = mintToken(client, ALICE_SECRET);
    assertTrue(aliceToken.verify().isSuccessful());

    // Bob creates nametag
    String bobNameTag = "bob@unicity-labs.com";
    byte[] bobStateData = "Bob's custom data".getBytes(StandardCharsets.UTF_8);

    // Alice transfers to Bob
    Transaction<TransferTransactionData> aliceToBobTransferTransaction = transferToken(
        client,
        ALICE_SECRET,
        aliceToken,
        ProxyAddress.create(bobNameTag),
        new DataHasher(HashAlgorithm.SHA256).update(bobStateData).digest()
    );

    // Bob prepares to receive the token

    byte[] bobNonce = randomBytes(32);
    SigningService bobSigningService = SigningService.createFromSecret(BOB_SECRET, bobNonce);
    MaskedPredicate bobPredicate = MaskedPredicate.create(bobSigningService, HashAlgorithm.SHA256,
        bobNonce);

    // Bob creates nametag
    Token<?> bobNametagToken = mintNametagToken(client, BOB_SECRET,
        bobNameTag,
        bobPredicate.getReference(aliceToken.getType()).toAddress());

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
  }
}