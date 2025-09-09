package org.unicitylabs.sdk.common;

import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TokenUtils;

/**
 * Alice has a nametag and acts as an escrow for the swap
 * Bob transfers token to Alice
 * Carol transfers token to Alice
 * <p>
 * Alice transfers Bob's token to Carol
 * Alice transfers Carol's token to Bob
 * <p>
 * Everyone's happy :)
 */
public abstract class BaseEscrowSwapTest {

  protected StateTransitionClient client;
  private final byte[] ALICE_SECRET = "ALICE_SECRET".getBytes(StandardCharsets.UTF_8);
  private final byte[] BOB_SECRET = "BOB_SECRET".getBytes(StandardCharsets.UTF_8);
  private final byte[] CAROL_SECRET = "CAROL_SECRET".getBytes(StandardCharsets.UTF_8);

  private final String ALICE_NAMETAG = String.format("ALICE_%s", System.currentTimeMillis());
  private final String BOB_NAMETAG = String.format("BOB_%s", System.currentTimeMillis());
  private final String CAROL_NAMETAG = String.format("CAROL_%s", System.currentTimeMillis());

  private String[] transferToken(Token<?> token, byte[] secret, String nametag) throws Exception {
    TransferCommitment commitment = TransferCommitment.create(
        token,
        ProxyAddress.create(nametag),
        randomBytes(32),
        null,
        null,
        SigningService.createFromSecret(secret, token.getState().getUnlockPredicate().getNonce())
    );

    SubmitCommitmentResponse response = this.client.submitCommitment(token, commitment).get();
    if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new RuntimeException("Failed to submit transfer commitment: " + response);
    }

    return new String[]{
        UnicityObjectMapper.JSON.writeValueAsString(token),
        UnicityObjectMapper.JSON.writeValueAsString(commitment.toTransaction(token,
            InclusionProofUtils.waitInclusionProof(client, commitment).get()))
    };
  }

  private Token<?> mintToken(byte[] secret) throws Exception {
    return TokenUtils.mintToken(
        this.client,
        secret,
        new TokenId(randomBytes(32)),
        new TokenType(HexConverter.decode(
            "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509")),
        randomBytes(32),
        null,
        randomBytes(32),
        randomBytes(32),
        null
    );
  }

  private Token<?> receiveToken(String[] tokenInfo, byte[] secret,
      NametagWrapper nametagToken) throws Exception {
    Token<?> token = UnicityObjectMapper.JSON.readValue(tokenInfo[0], Token.class);
    Transaction<TransferTransactionData> transaction = UnicityObjectMapper.JSON.readValue(
        tokenInfo[1],
        UnicityObjectMapper.JSON.getTypeFactory()
            .constructParametricType(Transaction.class, TransferTransactionData.class));

    byte[] nonce = randomBytes(32);
    TokenState state = new TokenState(
        MaskedPredicate.create(
            SigningService.createFromSecret(secret, nonce),
            HashAlgorithm.SHA256,
            nonce
        ),
        null
    );

    nametagToken.updateNameTag(this.client, secret,
        state.getUnlockPredicate().getReference(token.getType()).toAddress());

    return this.client.finalizeTransaction(
        token,
        state,
        transaction,
        List.of(nametagToken.getNametagToken())
    );
  }

  @Test
  void testEscrow() throws Exception {
    // Make nametags unique for each test run
    String[] bobSerializedData = transferToken(
        mintToken(BOB_SECRET),
        BOB_SECRET,
        ALICE_NAMETAG
    );
    String[] carolSerializedData = transferToken(
        mintToken(CAROL_SECRET),
        CAROL_SECRET,
        ALICE_NAMETAG
    );

    NametagWrapper aliceNametagToken = new NametagWrapper(
        TokenUtils.mintNametagToken(
            this.client,
            ALICE_SECRET,
            new TokenType(HexConverter.decode(
                "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509")),
            randomBytes(32),
            null,
            ALICE_NAMETAG,
            null,
            randomBytes(32),
            randomBytes(32)
        )
    );

    Token<?> aliceBobToken = receiveToken(bobSerializedData, ALICE_SECRET, aliceNametagToken);
    Assertions.assertTrue(aliceBobToken.verify().isSuccessful());
    Token<?> aliceCarolToken = receiveToken(carolSerializedData, ALICE_SECRET, aliceNametagToken);
    Assertions.assertTrue(aliceCarolToken.verify().isSuccessful());

    Token<?> aliceToCarolToken = receiveToken(
        transferToken(aliceBobToken, ALICE_SECRET, CAROL_NAMETAG),
        CAROL_SECRET,
        new NametagWrapper(
            TokenUtils.mintNametagToken(
                this.client,
                CAROL_SECRET,
                new TokenType(HexConverter.decode(
                    "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509")),
                randomBytes(32),
                null,
                CAROL_NAMETAG,
                null,
                randomBytes(32),
                randomBytes(32)
            )
        ));
    Assertions.assertTrue(aliceToCarolToken.verify().isSuccessful());

    Token<?> aliceToBobToken = receiveToken(
        transferToken(aliceCarolToken, ALICE_SECRET, BOB_NAMETAG),
        BOB_SECRET,
        new NametagWrapper(
            TokenUtils.mintNametagToken(
                this.client,
                BOB_SECRET,
                new TokenType(HexConverter.decode(
                    "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509")),
                randomBytes(32),
                null,
                BOB_NAMETAG,
                null,
                randomBytes(32),
                randomBytes(32)
            )
        ));
    Assertions.assertTrue(aliceToBobToken.verify().isSuccessful());
  }

  static class NametagWrapper {

    private Token<?> nametagToken;

    public NametagWrapper(Token<?> nametagToken) {
      this.nametagToken = nametagToken;
    }

    public Token<?> getNametagToken() {
      return this.nametagToken;
    }

    public void updateNameTag(StateTransitionClient client, byte[] secret, Address address)
        throws Exception {
      byte[] nonce = randomBytes(32);
      Predicate predicate = MaskedPredicate.create(
          SigningService.createFromSecret(secret, nonce),
          HashAlgorithm.SHA256,
          nonce
      );

      TransferCommitment commitment = TransferCommitment.create(
          this.nametagToken,
          predicate.getReference(this.nametagToken.getType()).toAddress(),
          randomBytes(32),
          new DataHasher(HashAlgorithm.SHA256)
              .update(address.getAddress().getBytes(StandardCharsets.UTF_8))
              .digest(),
          null,
          SigningService.createFromSecret(
              secret,
              this.nametagToken.getState().getUnlockPredicate().getNonce()
          )
      );

      SubmitCommitmentResponse response = client.submitCommitment(this.nametagToken, commitment)
          .get();
      if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
        throw new RuntimeException("Failed to submit transfer commitment: " + response);
      }

      this.nametagToken = client.finalizeTransaction(
          this.nametagToken,
          new TokenState(predicate, address.getAddress().getBytes(StandardCharsets.UTF_8)),
          commitment.toTransaction(
              this.nametagToken,
              InclusionProofUtils.waitInclusionProof(client, commitment).get()
          )
      );
    }
  }
}
