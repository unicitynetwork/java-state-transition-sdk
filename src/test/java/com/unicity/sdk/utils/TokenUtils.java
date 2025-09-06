package com.unicity.sdk.utils;

import static com.unicity.sdk.utils.TestUtils.randomBytes;
import static com.unicity.sdk.utils.TestUtils.randomCoinData;

import com.unicity.sdk.StateTransitionClient;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.SubmitCommitmentResponse;
import com.unicity.sdk.api.SubmitCommitmentStatus;
import com.unicity.sdk.hash.DataHash;
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
import com.unicity.sdk.util.InclusionProofUtils;

public class TokenUtils {
  public static Token<?> mintToken(StateTransitionClient client, byte[] secret) throws Exception {
    return TokenUtils.mintToken(
        client,
        secret,
        new TokenId(randomBytes(32)),
        new TokenType(randomBytes(32)),
        randomBytes(32),
        randomCoinData(2),
        randomBytes(32),
        randomBytes(32),
        null
    );
  }

  public static Token<?> mintToken(
      StateTransitionClient client,
      byte[] secret,
      TokenId tokenId,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      byte[] nonce,
      byte[] salt,
      DataHash dataHash
  ) throws Exception {
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
            tokenData,
            coinData,
            address,
            salt,
            dataHash,
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

  public static Token<?> mintNametagToken(
      StateTransitionClient client,
      byte[] secret,
      String nametag,
      Address targetAddress
  ) throws Exception {
    return mintNametagToken(
        client,
        secret,
        new TokenType(randomBytes(32)),
        randomBytes(32),
        randomCoinData(2),
        nametag,
        targetAddress,
        randomBytes(32),
        randomBytes(32)
    );
  }

  public static Token<?> mintNametagToken(
      StateTransitionClient client,
      byte[] secret,
      TokenType tokenType,
      byte[] tokenData,
      TokenCoinData coinData,
      String nametag,
      Address targetAddress,
      byte[] nonce,
      byte[] salt
  ) throws Exception {
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
            tokenData,
            coinData,
            address,
            salt,
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
}
