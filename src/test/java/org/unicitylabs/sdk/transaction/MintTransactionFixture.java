package org.unicitylabs.sdk.transaction;

public class MintTransactionFixture {
  public static MintTransaction create(
      MintTransaction.Data data,
      InclusionProof inclusionProof
  ) {
    return new MintTransaction(data, inclusionProof);
  }
}
