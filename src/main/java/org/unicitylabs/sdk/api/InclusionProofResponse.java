package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.transaction.InclusionProof;

public class InclusionProofResponse {

  private final InclusionProof inclusionProof;

  public InclusionProofResponse(InclusionProof inclusionProof) {
    this.inclusionProof = inclusionProof;
  }

  public InclusionProof getInclusionProof() {
    return this.inclusionProof;
  }
}
