package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.util.VerificationResult;

public interface MintTransactionReason {
  VerificationResult verify(Transaction<? extends MintTransactionData<?>> genesis);
}
