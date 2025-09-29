package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.verification.VerificationResult;

public interface MintTransactionReason {
  String getType();
  VerificationResult verify(MintTransaction<?> genesis);

  byte[] toCbor();
}
