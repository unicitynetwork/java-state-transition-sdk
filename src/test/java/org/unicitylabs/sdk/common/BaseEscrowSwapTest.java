package org.unicitylabs.sdk.common;

import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicateReference;
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
 * Alice has a nametag and acts as an escrow for the swap Bob transfers token to Alice Carol
 * transfers token to Alice
 * <p>
 * Alice transfers Bob's token to Carol Alice transfers Carol's token to Bob
 * <p>
 * Everyone's happy :)
 */
public abstract class BaseEscrowSwapTest {

  protected StateTransitionClient client;
  protected RootTrustBase trustBase;
  private final TokenType tokenType = new TokenType(HexConverter.decode(
      "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509"));


  private final byte[] ALICE_SECRET = "ALICE_SECRET".getBytes(StandardCharsets.UTF_8);
  private final byte[] BOB_SECRET = "BOB_SECRET".getBytes(StandardCharsets.UTF_8);
  private final byte[] CAROL_SECRET = "CAROL_SECRET".getBytes(StandardCharsets.UTF_8);

  private final String ALICE_NAMETAG = String.format("ALICE_%s", System.currentTimeMillis());
  private final String BOB_NAMETAG = String.format("BOB_%s", System.currentTimeMillis());
  private final String CAROL_NAMETAG = String.format("CAROL_%s", System.currentTimeMillis());

  private String[] transferToken(Token<?> token, SigningService signingService, String nametag)
      throws Exception {
    TransferCommitment commitment = TransferCommitment.create(
        token,
        ProxyAddress.create(nametag),
        randomBytes(32),
        null,
        null,
        signingService
    );

    SubmitCommitmentResponse response = this.client.submitCommitment(commitment).get();
    if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new RuntimeException("Failed to submit transfer commitment: " + response);
    }

    return new String[]{
        UnicityObjectMapper.JSON.writeValueAsString(token),
        UnicityObjectMapper.JSON.writeValueAsString(
            commitment.toTransaction(
                InclusionProofUtils.waitInclusionProof(
                    this.client,
                    this.trustBase,
                    commitment
                ).get()
            )
        )
    };
  }

  private Token<?> mintToken(byte[] secret) throws Exception {
    return TokenUtils.mintToken(
        this.client,
        this.trustBase,
        secret,
        new TokenId(randomBytes(32)),
        this.tokenType,
        randomBytes(32),
        null,
        randomBytes(32),
        randomBytes(32),
        null
    );
  }

  private Token<?> receiveToken(String[] tokenInfo, SigningService signingService,
      Token<?> nametagToken) throws Exception {
    Token<?> token = UnicityObjectMapper.JSON.readValue(tokenInfo[0], Token.class);
    Transaction<TransferTransactionData> transaction = UnicityObjectMapper.JSON.readValue(
        tokenInfo[1],
        UnicityObjectMapper.JSON.getTypeFactory()
            .constructParametricType(Transaction.class, TransferTransactionData.class));

    TokenState state = new TokenState(
        UnmaskedPredicate.create(
            token.getId(),
            token.getType(),
            signingService,
            HashAlgorithm.SHA256,
            transaction.getData().getSalt()
        ),
        null
    );

    return this.client.finalizeTransaction(
        this.trustBase,
        token,
        state,
        transaction,
        List.of(nametagToken)
    );
  }

  @Test
  void testEscrow() throws Exception {
    // Make nametags unique for each test run
    Token<?> bobToken = mintToken(BOB_SECRET);
    String[] bobSerializedData = this.transferToken(
        bobToken,
        SigningService.createFromMaskedSecret(
            BOB_SECRET,
            ((MaskedPredicate) bobToken.getState().getPredicate()).getNonce()
        ),
        ALICE_NAMETAG
    );

    Token<?> carolToken = mintToken(CAROL_SECRET);
    String[] carolSerializedData = this.transferToken(
        carolToken,
        SigningService.createFromMaskedSecret(
            CAROL_SECRET,
            ((MaskedPredicate) carolToken.getState().getPredicate()).getNonce()
        ),
        ALICE_NAMETAG
    );

    Token<?> aliceNametagToken = TokenUtils.mintNametagToken(
        this.client,
        this.trustBase,
        ALICE_SECRET,
        this.tokenType,
        ALICE_NAMETAG,
        UnmaskedPredicateReference.create(
            this.tokenType,
            SigningService.createFromSecret(ALICE_SECRET),
            HashAlgorithm.SHA256
        ).toAddress(),
        randomBytes(32),
        randomBytes(32)
    );

    Token<?> aliceBobToken = receiveToken(
        bobSerializedData,
        SigningService.createFromSecret(ALICE_SECRET),
        aliceNametagToken
    );
    Assertions.assertTrue(aliceBobToken.verify(this.trustBase).isSuccessful());
    Token<?> aliceCarolToken = receiveToken(
        carolSerializedData,
        SigningService.createFromSecret(ALICE_SECRET),
        aliceNametagToken
    );
    Assertions.assertTrue(aliceCarolToken.verify(this.trustBase).isSuccessful());

    Token<?> aliceToCarolToken = receiveToken(
        transferToken(
            aliceBobToken,
            SigningService.createFromSecret(ALICE_SECRET),
            CAROL_NAMETAG
        ),
        SigningService.createFromSecret(CAROL_SECRET),
        TokenUtils.mintNametagToken(
            this.client,
            this.trustBase,
            CAROL_SECRET,
            this.tokenType,
            CAROL_NAMETAG,
            UnmaskedPredicateReference.create(
                this.tokenType,
                SigningService.createFromSecret(CAROL_SECRET),
                HashAlgorithm.SHA256
            ).toAddress(),
            randomBytes(32),
            randomBytes(32)
        )
    );
    Assertions.assertTrue(aliceToCarolToken.verify(this.trustBase).isSuccessful());

    Token<?> aliceToBobToken = receiveToken(
        transferToken(
            aliceCarolToken,
            SigningService.createFromSecret(ALICE_SECRET),
            BOB_NAMETAG
        ),
        SigningService.createFromSecret(BOB_SECRET),
        TokenUtils.mintNametagToken(
            this.client,
            this.trustBase,
            BOB_SECRET,
            this.tokenType,
            BOB_NAMETAG,
            UnmaskedPredicateReference.create(
                this.tokenType,
                SigningService.createFromSecret(BOB_SECRET),
                HashAlgorithm.SHA256
            ).toAddress(),
            randomBytes(32),
            randomBytes(32)
        )
    );
    Assertions.assertTrue(aliceToBobToken.verify(this.trustBase).isSuccessful());
  }
}
