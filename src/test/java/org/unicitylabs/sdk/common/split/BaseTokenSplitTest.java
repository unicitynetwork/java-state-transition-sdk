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
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
import org.unicitylabs.sdk.predicate.UnmaskedPredicateReference;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.TransferCommitment;
import org.unicitylabs.sdk.transaction.split.SplitMintReason;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder;
import org.unicitylabs.sdk.transaction.split.TokenSplitBuilder.TokenSplit;
import org.unicitylabs.sdk.util.HexConverter;
import org.unicitylabs.sdk.util.InclusionProofUtils;
import org.unicitylabs.sdk.utils.TokenUtils;

public abstract class BaseTokenSplitTest {

  protected StateTransitionClient client;

  @Test
  void testTokenSplitFullAmounts() throws Exception {
    TokenType tokenType = new TokenType(HexConverter.decode(
        "f8aa13834268d29355ff12183066f0cb902003629bbc5eb9ef0efbe397867509"));
    byte[] secret = "SECRET".getBytes(StandardCharsets.UTF_8);

    Token<?> token = TokenUtils.mintToken(
        this.client,
        secret,
        new TokenId(randomBytes(32)),
        tokenType,
        randomBytes(32),
        new TokenCoinData(Map.of(
            new CoinId("test_eur".getBytes(StandardCharsets.UTF_8)),
            BigInteger.valueOf(100),
            new CoinId("test_usd".getBytes(StandardCharsets.UTF_8)),
            BigInteger.valueOf(100)
        )),
        randomBytes(32),
        randomBytes(32),
        null
    );

    String nametag = UUID.randomUUID().toString();

    Token<?> nametagToken = TokenUtils.mintNametagToken(
        this.client,
        secret,
        nametag,
        UnmaskedPredicateReference.create(
            tokenType,
            SigningService.createFromSecret(secret, null),
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
        SigningService.createFromSecret(secret, token.getState().getUnlockPredicate().getNonce())
    );

    SubmitCommitmentResponse burnCommitmentResponse = this.client
        .submitCommitment(token, burnCommitment)
        .get();

    if (burnCommitmentResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
      throw new Exception(String.format("Failed to submit burn commitment: %s",
          burnCommitmentResponse.getStatus()));
    }

    List<MintCommitment<MintTransactionData<SplitMintReason>>> mintCommitments = split.createSplitMintCommitments(
        burnCommitment.toTransaction(
            token,
            InclusionProofUtils.waitInclusionProof(this.client, burnCommitment).get()
        )
    );

    for (MintCommitment<MintTransactionData<SplitMintReason>> commitment : mintCommitments) {
      SubmitCommitmentResponse response = this.client
          .submitCommitment(commitment)
          .get();

      if (response.getStatus() != SubmitCommitmentStatus.SUCCESS) {
        throw new Exception(String.format("Failed to submit burn commitment: %s",
            response.getStatus()));
      }

      TokenState state = new TokenState(
          UnmaskedPredicate.create(
              SigningService.createFromSecret(secret, null),
              HashAlgorithm.SHA256,
              commitment.getTransactionData().getSalt()
          ),
          null
      );

      Token<MintTransactionData<SplitMintReason>> splitToken = new Token<>(
          state,
          commitment.toTransaction(
              InclusionProofUtils.waitInclusionProof(this.client, commitment).get()
          ),
          List.of(nametagToken)
      );

      Assertions.assertTrue(splitToken.verify().isSuccessful());
    }


  }


}
