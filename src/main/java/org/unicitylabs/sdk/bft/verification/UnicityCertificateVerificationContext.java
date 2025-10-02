package org.unicitylabs.sdk.bft.verification;

import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.hash.DataHash;

/**
 * Unicity certificate verification context.
 */
public class UnicityCertificateVerificationContext {

  private final DataHash inputHash;
  private final UnicityCertificate unicityCertificate;
  private final RootTrustBase trustBase;


  /**
   * Create unicity certificate verification context.
   *
   * @param inputHash          input record hash
   * @param unicityCertificate unicity certificate
   * @param trustBase          root trust base
   */
  public UnicityCertificateVerificationContext(
      DataHash inputHash,
      UnicityCertificate unicityCertificate,
      RootTrustBase trustBase
  ) {
    this.inputHash = inputHash;
    this.unicityCertificate = unicityCertificate;
    this.trustBase = trustBase;
  }

  /**
   * Get input record hash.
   *
   * @return input record hash
   */
  public DataHash getInputHash() {
    return this.inputHash;
  }

  /**
   * Get unicity certificate.
   *
   * @return unicity certificate
   */
  public UnicityCertificate getUnicityCertificate() {
    return this.unicityCertificate;
  }

  /**
   * Get root trust base.
   *
   * @return root trust base
   */
  public RootTrustBase getTrustBase() {
    return this.trustBase;
  }


}
