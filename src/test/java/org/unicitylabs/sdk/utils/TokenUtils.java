package org.unicitylabs.sdk.utils;

import static org.unicitylabs.sdk.utils.TestUtils.randomBytes;
import static org.unicitylabs.sdk.utils.TestUtils.randomCoinData;

import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.api.SubmitCommitmentResponse;
import org.unicitylabs.sdk.api.SubmitCommitmentStatus;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.MintCommitment;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.transaction.NametagMintTransactionData;
import org.unicitylabs.sdk.util.InclusionProofUtils;

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

    MintCommitment<NametagMintTransactionData<MintTransactionReason>> commitment = MintCommitment.create(
        new NametagMintTransactionData<>(
            nametag,
            tokenType,
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
        new TokenState(predicate, null),
        commitment.toTransaction(inclusionProof)
    );
  }
}
