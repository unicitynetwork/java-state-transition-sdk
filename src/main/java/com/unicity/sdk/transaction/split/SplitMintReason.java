
package com.unicity.sdk.transaction.split;

import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStepBranch;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep.Branch;
import com.unicity.sdk.predicate.BurnPredicate;
import com.unicity.sdk.predicate.PredicateType;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.util.VerificationResult;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SplitMintReason implements MintTransactionReason {

  private final Token<?> token;
  private final List<SplitMintReasonProof> proofs;

  public SplitMintReason(Token<?> token, List<SplitMintReasonProof> proofs) {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(proofs, "Proofs cannot be null");

    this.token = token;
    this.proofs = List.copyOf(proofs);
  }

  public Token<?> getToken() {
    return this.token;
  }

  public List<SplitMintReasonProof> getProofs() {
    return List.copyOf(this.proofs);
  }

  public VerificationResult verify(Transaction<? extends MintTransactionData<?>> transaction) {
    if (!transaction.getData().getCoinData().isPresent()) {
      return VerificationResult.fail("Coin data is missing.");
    }

    if (!PredicateType.BURN.name().equals(this.token.getState().getUnlockPredicate().getType())) {
      return VerificationResult.fail("Token is not burned");
    }

    Map<CoinId, BigInteger> coins = transaction.getData().getCoinData().map(TokenCoinData::getCoins)
        .orElse(Map.of());
    if (coins.size() != this.proofs.size()) {
      return VerificationResult.fail("Total amount of coins differ in token and proofs.");
    }

    for (SplitMintReasonProof proof : this.proofs) {
      if (!proof.getAggregationPath().verify(proof.getCoinId().toBitString().toBigInteger())
          .isValid()) {
        return VerificationResult.fail(
            "Aggregation path verification failed for coin: " + proof.getCoinId());
      }

      if (!proof.getCoinTreePath()
          .verify(transaction.getData().getTokenId().toBitString().toBigInteger()).isValid()) {
        return VerificationResult.fail(
            "Coin tree path verification failed for token");
      }

      List<SparseMerkleTreePathStep> aggregationPathSteps = proof.getAggregationPath()
          .getSteps();
      if (aggregationPathSteps.isEmpty()
          || !Arrays.equals(
          proof.getCoinTreePath().getRoot().getHash().getImprint(),
          aggregationPathSteps.get(0).getBranch()
              .map(SparseMerkleTreePathStepBranch::getValue)
              .orElse(null))
      ) {
        return VerificationResult.fail("Coin tree root does not match aggregation path leaf.");
      }

      if (!proof.getCoinTreePath().getSteps().get(0).getBranch()
          .map(Branch::getCounter).equals(Optional.ofNullable(coins.get(proof.getCoinId())))) {
        return VerificationResult.fail("Coin amount in token does not match coin tree leaf.");
      }

      BurnPredicate predicate = (BurnPredicate) this.token.getState().getUnlockPredicate();
      if (!proof.getAggregationPath().getRootHash().equals(predicate.getReason())) {
        return VerificationResult.fail("Burn reason does not match aggregation root.");
      }
    }

    return VerificationResult.success();
  }
}
