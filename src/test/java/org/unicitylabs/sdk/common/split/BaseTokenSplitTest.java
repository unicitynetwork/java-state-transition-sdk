package org.unicitylabs.sdk.common.split;

import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.ProxyAddress;
import org.unicitylabs.sdk.api.CertificationResponse;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
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
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintReasonFactory;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder.TokenSplit;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TokenUtils;

public abstract class BaseTokenSplitTest {

  protected StateTransitionClient client;
  protected RootTrustBase trustBase;

  private final MintReasonFactory mintReasonFactory = new DefaultMintReasonFactory();

  @Test
  void testTokenSplitFullAmounts() throws Exception {
    TokenType tokenType = new TokenType(HexConverter.decode(
        "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509"));
    byte[] secret = "SECRET".getBytes(StandardCharsets.UTF_8);

    Token token = TokenUtils.mintToken(
        this.client,
        this.trustBase,
        this.mintReasonFactory,
        secret,
        new TokenId(randomBytes(32)),
        tokenType,
        randomBytes(32),
        new TokenCoinData(
            Map.of(
                new CoinId("test_eur".getBytes(StandardCharsets.UTF_8)),
                BigInteger.valueOf(100),
                new CoinId("test_usd".getBytes(StandardCharsets.UTF_8)),
                BigInteger.valueOf(100)
            )
        ),
        randomBytes(32),
        randomBytes(32),
        null
    );

    String nametag = UUID.randomUUID().toString();

    Token nametagToken = TokenUtils.mintNametagToken(
        this.client,
        this.trustBase,
        this.mintReasonFactory,
        secret,
        nametag,
        UnmaskedPredicateReference.create(
            tokenType,
            SigningService.createFromSecret(secret),
            HashAlgorithm.SHA256
        ).toAddress()
    );

    TokenSplitBuilder builder = new TokenSplitBuilder();
    TokenSplit split = builder
        .createToken(
            new TokenId(randomBytes(32)),
            tokenType,
            null,
            new TokenCoinData(Map.of(
                new CoinId("test_eur".getBytes(StandardCharsets.UTF_8)),
                BigInteger.valueOf(100)
            )),
            ProxyAddress.create(nametag),
            randomBytes(32),
            null
        )
        .createToken(
            new TokenId(randomBytes(32)),
            tokenType,
            null,
            new TokenCoinData(Map.of(
                new CoinId("test_usd".getBytes(StandardCharsets.UTF_8)),
                BigInteger.valueOf(100)
            )),
            ProxyAddress.create(nametag),
            randomBytes(32),
            null
        )
        .build(token);

    TransferCommitment burnCommitment = split.createBurnCommitment(
        randomBytes(32),
        SigningService.createFromMaskedSecret(
            secret,
            ((MaskedPredicate) token.getState().getPredicate()).getNonce()
        )
    );

    CertificationResponse burnCommitmentResponse = this.client
        .submitCommitment(burnCommitment)
        .get();

    if (burnCommitmentResponse.getStatus() != CertificationStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit burn commitment: %s",
          burnCommitmentResponse.getStatus()));
    }

    List<MintCommitment> mintCommitments = split.createSplitMintCommitments(
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

    for (MintCommitment commitment : mintCommitments) {
      CertificationResponse response = this.client
          .submitCommitment(commitment)
          .get();

      if (response.getStatus() != CertificationStatus.SUCCESS) {
        throw new Exception(String.format("Failed to submit burn commitment: %s",
            response.getStatus()));
      }

      MintTransaction transaction = commitment.toTransaction(
          InclusionProofUtils.waitInclusionProof(this.client, this.trustBase, commitment).get()
      );

      TokenState state = new TokenState(
          UnmaskedPredicate.create(
              commitment.getTransactionData().getTokenId(),
              commitment.getTransactionData().getTokenType(),
              transaction,
              SigningService.createFromSecret(secret),
              HashAlgorithm.SHA256
          ),
          null
      );

      Token splitToken = Token.mint(
          this.trustBase,
          this.mintReasonFactory,
          state,
          transaction,
          List.of(nametagToken)
      );

      Assertions.assertTrue(splitToken.verify(this.trustBase, this.mintReasonFactory).isSuccessful());
    }


  }


}
