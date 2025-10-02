package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;

public class InclusionProofFixture {
  public static InclusionProof create(
      SparseMerkleTreePath path,
      Authenticator authenticator,
      DataHash transactionHash,
      UnicityCertificate certificate
  ) {
    return new InclusionProof(path, authenticator, transactionHash, certificate);
  }
}
