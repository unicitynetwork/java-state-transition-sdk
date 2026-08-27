package org.unicitylabs.sdk.transaction.verification;

/**
 * Status codes returned by inclusion proof verification.
 */
public enum InclusionProofVerificationStatus {
  /** The provided trust base is invalid or cannot be used for verification. */
  INVALID_TRUSTBASE,
  /** Certification lock script or source state hash does not match the reconstructed transaction. */
  CERTIFICATION_DATA_MISMATCH,
  /** Transaction hash does not match the value referenced by the proof. */
  TRANSACTION_HASH_MISMATCH,
  /** The round's reference time had already reached the request's timeout. */
  /**
   * The leaf claims a reference time later than the round that certified it, which no honest
   * service can produce.
   */
  REFERENCE_TIME_AFTER_ROUND,
  REQUEST_EXPIRED,
  /** Proof authentication failed. */
  NOT_AUTHENTICATED,
  /** Proof path is not included in the committed tree state. */
  PATH_NOT_INCLUDED,

  /** Proof path structure or hashes are invalid. */
  PATH_INVALID,
  /** Shard id of the unicity certificate does not match the transaction state id. */
  SHARD_ID_MISMATCH,
  /** Inclusion proof verification succeeded. */
  OK
}
