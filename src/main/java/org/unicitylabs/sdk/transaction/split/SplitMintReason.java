
package org.unicitylabs.sdk.transaction.split;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineService;
import org.unicitylabs.sdk.predicate.embedded.BurnPredicate;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.MintReasonType;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import org.unicitylabs.sdk.verification.VerificationResult;

/**
 * Mint reason for splitting a token.
 */
@JsonIgnoreProperties()
public class SplitMintReason implements MintTransactionReason {

  private final Token<?> token;
  private final List<SplitMintReasonProof> proofs;

  @JsonCreator
  SplitMintReason(
      @JsonProperty("token") Token<?> token,
      @JsonProperty("proofs") List<SplitMintReasonProof> proofs
  ) {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(proofs, "Proofs cannot be null");

    this.token = token;
    this.proofs = List.copyOf(proofs);
  }

  /**
   * Get mint reason type.
   *
   * @return token split reason
   */
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  public String getType() {
    return MintReasonType.TOKEN_SPLIT.name();
  }

  /**
   * Get token which was burnt for split.
   *
   * @return burnt token
   */
  public Token<?> getToken() {
    return this.token;
  }

  /**
   * Get proofs for currently minted token coins.
   *
   * @return split proofs
   */
  public List<SplitMintReasonProof> getProofs() {
    return List.copyOf(this.proofs);
  }

  /**
   * Verify mint transaction against mint reason.
   *
   * @param transaction Genesis to verify against
   * @return verification result
   */
  public VerificationResult verify(MintTransaction<?> transaction) {
    if (!transaction.getData().getCoinData().isPresent()) {
      return VerificationResult.fail("Coin data is missing.");
    }

    Predicate predicate = PredicateEngineService.createPredicate(
        this.token.getState().getPredicate());
    if (!(predicate instanceof BurnPredicate)) {
      return VerificationResult.fail("Token is not burned");
    }

    Map<CoinId, BigInteger> coins = transaction.getData().getCoinData().map(TokenCoinData::getCoins)
        .orElse(Map.of());
    if (coins.size() != this.proofs.size()) {
      return VerificationResult.fail("Total amount of coins differ in token and proofs.");
    }

    for (SplitMintReasonProof proof : this.proofs) {
      if (!proof.getAggregationPath().verify(proof.getCoinId().toBitString().toBigInteger())
          .isSuccessful()) {
        return VerificationResult.fail(
            "Aggregation path verification failed for coin: " + proof.getCoinId());
      }

      if (!proof.getCoinTreePath()
          .verify(transaction.getData().getTokenId().toBitString().toBigInteger()).isSuccessful()) {
        return VerificationResult.fail(
            "Coin tree path verification failed for token");
      }

      List<SparseMerkleTreePathStep> aggregationPathSteps = proof.getAggregationPath()
          .getSteps();
      if (aggregationPathSteps.size() == 0
          || !Arrays.equals(proof.getCoinTreePath().getRootHash().getImprint(),
          aggregationPathSteps.get(0).getData().orElse(null))
      ) {
        return VerificationResult.fail("Coin tree root does not match aggregation path leaf.");
      }

      if (!proof.getCoinTreePath().getSteps().get(0).getValue().equals(coins.get(proof.getCoinId()))) {
        return VerificationResult.fail("Coin amount in token does not match coin tree leaf.");
      }

      if (!proof.getAggregationPath().getRootHash()
          .equals(((BurnPredicate) predicate).getReason())) {
        return VerificationResult.fail("Burn reason does not match aggregation root.");
      }
    }

    return VerificationResult.success();
  }

  /**
   * Create split mint reason from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return mint reason
   */
  public static SplitMintReason fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SplitMintReason(
        Token.fromCbor(data.get(1)),
        CborDeserializer.readArray(data.get(2)).stream()
            .map(SplitMintReasonProof::fromCbor)
            .collect(Collectors.toList())
    );
  }

  /**
   * Convert split mint reason to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeTextString(this.getType()),
        this.token.toCbor(),
        CborSerializer.encodeArray(
            this.proofs.stream()
                .map(SplitMintReasonProof::toCbor)
                .toArray(byte[][]::new)
        )
    );
  }

  @Override
  public String toString() {
    return String.format("SplitMintReason{token=%s, proofs=%s}", this.token, this.proofs);
  }
}
