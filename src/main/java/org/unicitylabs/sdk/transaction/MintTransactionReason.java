package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.util.VerificationResult;

public interface MintTransactionReason {
  String getType();
  VerificationResult verify(Transaction<? extends MintTransactionData<?>> genesis);
}
