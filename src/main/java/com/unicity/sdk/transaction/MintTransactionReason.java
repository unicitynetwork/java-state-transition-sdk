package com.unicity.sdk.transaction;

public interface MintTransactionReason {
  boolean verify(Transaction<? extends MintTransactionData<?>> genesis);
}
