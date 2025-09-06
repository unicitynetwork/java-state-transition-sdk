package com.unicity.sdk.common.split;

import static com.unicity.sdk.utils.TestUtils.randomBytes;

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
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintCommitment;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.TransferCommitment;
import com.unicity.sdk.transaction.split.SplitMintReason;
import com.unicity.sdk.transaction.split.TokenSplitBuilder;
import com.unicity.sdk.transaction.split.TokenSplitBuilder.TokenSplit;
import com.unicity.sdk.util.InclusionProofUtils;
import com.unicity.sdk.utils.TokenUtils;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public abstract class TokenSplitTest {

  protected StateTransitionClient client;

  @Test
  void testTokenSplitFullAmounts() throws Exception {
    byte[] secret = "SECRET".getBytes(StandardCharsets.UTF_8);

    Token<?> token = TokenUtils.mintToken(
        this.client,
        secret,
        new TokenId(randomBytes(32)),
        new TokenType(randomBytes(32)),
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

    String nametag = "my_nametag";

    Token<?> nametagToken = TokenUtils.mintNametagToken(
        this.client,
        secret,
        nametag,
        DirectAddress.create(DataHash.fromImprint(new byte[34]))
    );

    TokenSplitBuilder builder = new TokenSplitBuilder();
    TokenSplit split = builder
        .createToken(
            new TokenId(randomBytes(32)),
            new TokenType(randomBytes(32)),
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
            new TokenType(randomBytes(32)),
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

      byte[] nonce = randomBytes(32);
      TokenState state = new TokenState(
          MaskedPredicate.create(
              SigningService.createFromSecret(secret, nonce),
              HashAlgorithm.SHA256,
              nonce
          ),
          null
      );
      Address address = state.getUnlockPredicate()
          .getReference(
              commitment.getTransactionData().getTokenType()
          )
          .toAddress();

      byte[] nametagNonce = randomBytes(32);
      TokenState nametagTokenState = new TokenState(
          MaskedPredicate.create(
              SigningService.createFromSecret(secret, nametagNonce),
              HashAlgorithm.SHA256,
              nametagNonce
          ),
          address.getAddress().getBytes(StandardCharsets.UTF_8)
      );

      TransferCommitment nametagCommitment = TransferCommitment.create(
          nametagToken,
          nametagTokenState.getUnlockPredicate().getReference(nametagToken.getType()).toAddress(),
          randomBytes(32),
          new DataHasher(HashAlgorithm.SHA256)
              .update(address.getAddress().getBytes(StandardCharsets.UTF_8))
              .digest(),
          null,
          SigningService.createFromSecret(
              secret,
              nametagToken.getState().getUnlockPredicate().getNonce()
          )
      );

      SubmitCommitmentResponse nametagTransferResponse = this.client
          .submitCommitment(nametagToken, nametagCommitment).get();
      if (nametagTransferResponse.getStatus() != SubmitCommitmentStatus.SUCCESS) {
        throw new Exception(String.format("Failed to submit burn commitment: %s",
            response.getStatus()));
      }

      nametagToken = this.client.finalizeTransaction(
          nametagToken,
          nametagTokenState,
          nametagCommitment.toTransaction(
              nametagToken,
              InclusionProofUtils.waitInclusionProof(this.client, nametagCommitment).get()
          )
      );

      Token<MintTransactionData<SplitMintReason>> splitToken = new Token<>(
          state,
          commitment.toTransaction(
              InclusionProofUtils.waitInclusionProof(this.client, commitment).get()),
          List.of(nametagToken)
      );

      Assertions.assertTrue(splitToken.verify().isSuccessful());
    }


  }


}
