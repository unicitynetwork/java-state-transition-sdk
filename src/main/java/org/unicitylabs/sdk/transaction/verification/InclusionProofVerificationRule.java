package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.InclusionProof;
import org.unicitylabs.sdk.api.LeafValue;
import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.api.bft.RootTrustBase;
import org.unicitylabs.sdk.api.bft.verification.UnicityCertificateVerification;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.util.verification.VerificationResult;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

/**
 * This class provides the functionality to verify an inclusion proof against a given trust base
 * and transaction. It ensures that the inclusion proof is valid, authentic, and corresponds to
 * the specified transaction.
 *
 *  <p>The verification process involves several checks, including:
 * - Validating the trust base against the inclusion proof.
 * - Ensuring the Merkle tree path is valid and included in the committed tree.
 * - Verifying the certification data referenced by the inclusion proof.
 * - Checking that the transaction hash matches the reference in the proof.
 * - Confirming the proof's leaf value aligns with the expected hash.
 * - Verifies given predicate against certification data
 */
public class InclusionProofVerificationRule {

  /**
   * Verifies the provided inclusion proof against the specified trust base and transaction.
   *
   * @param trustBase the root trust base used to validate the inclusion proof
   * @param predicateVerifier the service responsible for evaluating transaction predicates
   * @param inclusionProof the inclusion proof containing certification data and merkle tree path
   * @param transaction the transaction that is being verified against the proof
   * @param referenceTime reference time the transition was validated under
   *
   * @return a {@code VerificationResult} object containing the {@code InclusionProofVerificationStatus}
   *         and additional details about the verification outcome
   */
  public static VerificationResult<InclusionProofVerificationStatus> verify(RootTrustBase trustBase,
                                                                            PredicateVerifierService predicateVerifier, InclusionProof inclusionProof,
                                                                            Transaction transaction, long referenceTime) {
    if (inclusionProof.getInclusionCertificate() == null) {
      return new VerificationResult<>(
              "InclusionProofVerificationRule",
              InclusionProofVerificationStatus.INCLUSION_CERTIFICATE_MISSING
      );
    }

    CertificationData certificationData = inclusionProof.getCertificationData().orElse(null);
    if (certificationData == null) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.MISSING_CERTIFICATION_DATA);
    }

    if (!certificationData.getTransactionHash().equals(transaction.calculateTransactionHash())) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.TRANSACTION_HASH_MISMATCH);
    }

    if (!certificationData.getLockScript().equals(transaction.getLockScript())
            || !certificationData.getSourceStateHash().equals(transaction.getSourceStateHash())
            || !certificationData.getExpiresAt().equals(transaction.getExpiresAt())) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.CERTIFICATION_DATA_MISMATCH);
    }

    // The request was admissible only in a round strictly before its deadline. A request that
    // carried no deadline was admitted under a service-assigned one, which is not recorded and is
    // not re-checked here.
    if (transaction.getExpiresAt().isPresent() && referenceTime >= transaction.getExpiresAt().get()) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.REQUEST_EXPIRED);
    }

    if (!inclusionProof.getReferenceTime().isPresent()
            || inclusionProof.getReferenceTime().get() != referenceTime) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.MISSING_REFERENCE_TIME);
    }

    StateId stateId = StateId.fromTransaction(transaction);
    // The leaf value binds the reference time the transition was validated under. It is taken
    // from the caller, not from the proof's own unicity certificate: the tree is append-only,
    // so the proof may have been issued against a later root whose input record carries a
    // later reference time.
    DataHash leafValue = LeafValue.calculate(certificationData.getTransactionHash(), referenceTime);
    if (!inclusionProof.getInclusionCertificate().verify(stateId, leafValue, new DataHash(HashAlgorithm.SHA256, inclusionProof.getUnicityCertificate().getInputRecord().getHash()))) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.PATH_INVALID);
    }

    VerificationResult<?> result = ShardIdMatchesStateIdRule.verify(
            stateId,
            inclusionProof.getUnicityCertificate().getShardTreeCertificate()
    );
    if (result.getStatus() != VerificationStatus.OK) {
      return new VerificationResult<>(
              "InclusionProofVerificationRule",
              InclusionProofVerificationStatus.SHARD_ID_MISMATCH,
              "",
              result
      );
    }

    result = UnicityCertificateVerification.verify(trustBase, inclusionProof);
    if (result.getStatus() != VerificationStatus.OK) {
      return new VerificationResult<>(
              "InclusionProofVerificationRule",
              InclusionProofVerificationStatus.INVALID_TRUSTBASE,
              "",
              result
      );
    }

    result = predicateVerifier.verify(
            transaction.getLockScript(),
            referenceTime,
            transaction.getSourceStateHash(),
            certificationData.getTransactionHash(),
            certificationData.getUnlockScript()
    );

    if (result.getStatus() != VerificationStatus.OK) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.NOT_AUTHENTICATED, "", result);
    }

    return new VerificationResult<>("InclusionProofVerificationRule",
            InclusionProofVerificationStatus.OK);
  }
}
