package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.predicate.builtin.BurnPredicate;
import org.unicitylabs.sdk.transaction.TransferTransaction;

import java.util.List;

/**
 * Result of token split generation containing the burn owner predicate, the burn transaction and
 * per-output split tokens.
 */
public class SplitResult {

  private final BurnPredicate burnPredicate;
  private final TransferTransaction burnTransaction;
  private final List<SplitToken> tokens;

  SplitResult(BurnPredicate burnPredicate, TransferTransaction burnTransaction,
              List<SplitToken> tokens) {
    this.burnPredicate = burnPredicate;
    this.burnTransaction = burnTransaction;
    this.tokens = List.copyOf(tokens);
  }

  /**
   * Get the burn owner predicate committing to the split manifest hash.
   *
   * @return burn predicate
   */
  public BurnPredicate getBurnPredicate() {
    return this.burnPredicate;
  }

  /**
   * Get the burn transaction that anchors split proofs.
   *
   * @return burn transaction
   */
  public TransferTransaction getBurnTransaction() {
    return this.burnTransaction;
  }

  /**
   * Get the split tokens ready to be minted.
   *
   * @return immutable list of split tokens
   */
  public List<SplitToken> getTokens() {
    return this.tokens;
  }
}
