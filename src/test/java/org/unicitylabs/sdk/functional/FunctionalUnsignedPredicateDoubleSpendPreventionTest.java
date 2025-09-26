package org.unicitylabs.sdk.functional;

import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.BranchExistsException;
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
import org.unicitylabs.sdk.utils.RootTrustBaseUtils;
import org.unicitylabs.sdk.utils.TokenUtils;

public class FunctionalUnsignedPredicateDoubleSpendPreventionTest {
  protected StateTransitionClient client;
  protected RootTrustBase trustBase;

  private final byte[] BOB_SECRET = "BOB_SECRET".getBytes(StandardCharsets.UTF_8);

  private String[] transferToken(Token<?> token, byte[] secret, Address address) throws Exception {
    TransferCommitment commitment = TransferCommitment.create(
        token,
        address,
        randomBytes(32),
        null,
        null,
        SigningService.createFromMaskedSecret(
            secret,
            ((MaskedPredicate) token.getState().getPredicate()).getNonce()
        )
    );

    SubmitCommitmentResponse response = this.client.submitCommitment(commitment).get();
    if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new RuntimeException("Failed to submit transfer commitment: " + response);
    }

    return new String[]{
        UnicityObjectMapper.JSON.writeValueAsString(token),
        UnicityObjectMapper.JSON.writeValueAsString(
            commitment.toTransaction(
                InclusionProofUtils.waitInclusionProof(this.client, this.trustBase, commitment).get()
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
        new TokenType(HexConverter.decode(
            "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509")),
        randomBytes(32),
        null,
        randomBytes(32),
        randomBytes(32),
        null
    );
  }

  private Token<?> receiveToken(String[] tokenInfo, byte[] secret) throws Exception {
    Token<?> token = UnicityObjectMapper.JSON.readValue(tokenInfo[0], Token.class);
    Transaction<TransferTransactionData> transaction = UnicityObjectMapper.JSON.readValue(
        tokenInfo[1],
        UnicityObjectMapper.JSON.getTypeFactory()
            .constructParametricType(Transaction.class, TransferTransactionData.class));

    TokenState state = new TokenState(
        UnmaskedPredicate.create(
            token.getId(),
            token.getType(),
            SigningService.createFromSecret(secret),
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
        List.of()
    );
  }

  @BeforeEach
  void setUp() {
    SigningService signingService = new SigningService(SigningService.generatePrivateKey());
    this.client = new StateTransitionClient(new TestAggregatorClient(signingService));
    this.trustBase = RootTrustBaseUtils.generateRootTrustBase(signingService.getPublicKey());
  }

  @Test
  void testDoubleSpend() throws Exception {
    Token<?> token = mintToken(BOB_SECRET);

    UnmaskedPredicateReference reference = UnmaskedPredicateReference.create(
        token.getType(),
        SigningService.createFromSecret(BOB_SECRET),
        HashAlgorithm.SHA256
    );

    Assertions.assertTrue(
        receiveToken(
            transferToken(token, BOB_SECRET, reference.toAddress()),
            BOB_SECRET
        ).verify(trustBase).isSuccessful());
    RuntimeException ex = Assertions.assertThrows(
        RuntimeException.class,
        () -> receiveToken(
            transferToken(token, BOB_SECRET, reference.toAddress()),
            BOB_SECRET
        ).verify(trustBase)
    );

    Assertions.assertInstanceOf(BranchExistsException.class, ex.getCause());
  }
}
