package org.unicitylabs.sdk.transaction;

public class MintTransactionFixture {
  public static <R extends MintTransactionReason> MintTransaction<R> create(
      MintTransaction.Data<R> data,
      InclusionProof inclusionProof
  ) {
    return new MintTransaction<>(data, inclusionProof);
  }
}
