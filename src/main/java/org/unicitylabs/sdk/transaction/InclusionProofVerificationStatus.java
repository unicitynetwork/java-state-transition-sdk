package org.unicitylabs.sdk.transaction;

/**
 * Status codes for verifying an InclusionProof.
 */
public enum InclusionProofVerificationStatus {
  /**
   * Inclusion proof verification failed because the trust base is invalid.
   */
  INVALID_TRUST_BASE("INVALID_TRUST_BASE"),
  /**
   * Inclusion proof verification failed because the proof could not be authenticated.
   */
  NOT_AUTHENTICATED("NOT_AUTHENTICATED"),
  /**
   * Inclusion proof verification failed because the path is not included in the Merkle tree.
   */
  PATH_NOT_INCLUDED("PATH_NOT_INCLUDED"),
  /**
   * Inclusion proof verification failed because the path is invalid.
   */
  PATH_INVALID("PATH_INVALID"),
  /**
   * Inclusion proof verification succeeded.
   */
  OK("OK");

  private final String value;

  InclusionProofVerificationStatus(String value) {
    this.value = value;
  }

  /**
   * Get inclusion proof verification status value.
   *
   * @return status value
   */
  public String getValue() {
    return value;
  }
}