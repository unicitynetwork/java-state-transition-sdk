package org.unicitylabs.sdk.bft.verification;

import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.hash.DataHash;

public class UnicityCertificateVerificationContext {

  private final DataHash inputHash;
  private final UnicityCertificate unicityCertificate;
  private final RootTrustBase trustBase;


  public UnicityCertificateVerificationContext(
      DataHash inputHash,
      UnicityCertificate unicityCertificate,
      RootTrustBase trustBase
  ) {
    this.inputHash = inputHash;
    this.unicityCertificate = unicityCertificate;
    this.trustBase = trustBase;
  }

  public DataHash getInputHash() {
    return this.inputHash;
  }

  public UnicityCertificate getUnicityCertificate() {
    return this.unicityCertificate;
  }

  public RootTrustBase getTrustBase() {
    return this.trustBase;
  }


}
