
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.predicate.BurnPredicate;
import com.unicity.sdk.predicate.PredicateType;
import com.unicity.sdk.smt.path.MerkleTreePathStep;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.Transaction;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SplitMintReason implements MintTransactionReason {

  private final Token<?> token;
  private final Map<CoinId, SplitMintReasonProof> proofs;

  public SplitMintReason(Token<?> token) {
    this.token = token;
    this.proofs = Map.of();
  }

  public boolean verify(Transaction<MintTransactionData<?>> transaction) {
    if (transaction.getData().getCoinData() == null) {
      return false;
    }

    if (PredicateType.valueOf(this.token.getState().getUnlockPredicate().getType())
        != PredicateType.BURN) {
      return false;
    }

    Map<CoinId, BigInteger> coins = transaction.getData().getCoinData().getCoins();
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

      List<MerkleTreePathStep> aggregationPathSteps = proof.getValue().getAggregationPath()
          .getSteps();
      if (aggregationPathSteps.isEmpty()
          || aggregationPathSteps.getFirst().getBranch() == null
          || aggregationPathSteps.getFirst().getBranch().getValue() == null
          || !proof.getValue().getCoinTreePath().getRootHash()
          .equals(DataHash.fromImprint(aggregationPathSteps.getFirst().getBranch().getValue()))) {
        return false;
      }

//
//        const sumPathLeaf = proof.coinTreePath.steps.at(0) ?.branch ?.sum;
//      if (coins.get(coinId) != = sumPathLeaf) {
//        return false;
//      }

      if (PredicateType.valueOf(this.token.getState().getUnlockPredicate().getType())
          != PredicateType.BURN) {
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
