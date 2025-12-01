package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;

public class InclusionProofFixture {
  public static InclusionProof create(
      SparseMerkleTreePath path,
      CertificationData certificationData,
      UnicityCertificate certificate
  ) {
    return new InclusionProof(path, certificationData, certificate);
  }
}
