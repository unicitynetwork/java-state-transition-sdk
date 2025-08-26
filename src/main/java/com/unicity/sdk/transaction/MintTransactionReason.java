package com.unicity.sdk.transaction;

import com.unicity.sdk.util.VerificationResult;

public interface MintTransactionReason {
  VerificationResult verify(Transaction<? extends MintTransactionData<?>> genesis);
}
