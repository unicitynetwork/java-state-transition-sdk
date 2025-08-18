
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import com.unicity.sdk.mtree.sum.SparseMerkleSumTreePathStep.Branch;
import com.unicity.sdk.predicate.BurnPredicate;
import com.unicity.sdk.predicate.PredicateType;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.Transaction;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

public class SplitMintReason implements MintTransactionReason {

  private final Token<?> token;
  private final Map<CoinId, SplitMintReasonProof> proofs;

  public SplitMintReason(Token<?> token, Map<CoinId, SplitMintReasonProof> proofs) {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(proofs, "Proofs cannot be null");

    this.token = token;
    this.proofs = Map.copyOf(proofs);
  }

  public Token<?> getToken() {
    return this.token;
  }

  public Map<CoinId, SplitMintReasonProof> getProofs() {
    return Map.copyOf(this.proofs);
  }

  public boolean verify(Transaction<? extends MintTransactionData<?>> transaction) {
    if (transaction.getData().getCoinData().isEmpty()) {
      return false;
    }

    if (!PredicateType.BURN.name().equals(this.token.getState().getUnlockPredicate().getType())) {
      return false;
    }

    Map<CoinId, BigInteger> coins = transaction.getData().getCoinData().map(TokenCoinData::getCoins)
        .orElse(Map.of());
    if (coins.size() != this.proofs.size()) {
      return false;
    }

    for (Entry<CoinId, SplitMintReasonProof> proof : this.proofs.entrySet()) {
      if (!proof.getValue().getAggregationPath().verify(proof.getKey().toBitString().toBigInteger())
          .isValid()) {
        return false;
      }

      if (!proof.getValue().getCoinTreePath()
          .verify(transaction.getData().getTokenId().toBitString().toBigInteger()).isValid()) {
        return false;
      }

      List<SparseMerkleTreePathStep> aggregationPathSteps = proof.getValue().getAggregationPath()
          .getSteps();
      if (aggregationPathSteps.isEmpty()
          || aggregationPathSteps.getFirst().getBranch() == null
          || aggregationPathSteps.getFirst().getBranch().getValue() == null
          || !proof.getValue().getCoinTreePath().getRoot().getHash()
          .equals(DataHash.fromImprint(aggregationPathSteps.getFirst().getBranch().getValue()))) {
        return false;
      }

      if (!proof.getValue().getCoinTreePath().getSteps().getFirst().getBranch()
          .map(Branch::getCounter).equals(Optional.ofNullable(coins.get(proof.getKey())))) {
        return false;
      }

      BurnPredicate predicate = (BurnPredicate) this.token.getState().getUnlockPredicate();
      if (!proof.getValue().getAggregationPath().getRootHash().equals(predicate.getReason())) {
        return false;
      }
    }

    return true;
  }
}
