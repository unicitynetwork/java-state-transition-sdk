package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.verification.VerificationResult;

public interface MintTransactionReason {
  String getType();
  VerificationResult verify(Transaction<? extends MintTransactionData<?>> genesis);
}
