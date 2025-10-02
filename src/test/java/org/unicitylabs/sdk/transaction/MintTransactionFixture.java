package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;

public class MintTransactionFixture {
  public static <R extends MintTransactionReason> MintTransaction<R> create(
      MintTransaction.Data<R> data,
      InclusionProof inclusionProof
  ) {
    return new MintTransaction<>(data, inclusionProof);
  }
}
