package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.InclusionCertificate;
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
   *
   * @return a {@code VerificationResult} object containing the {@code InclusionProofVerificationStatus}
   *         and additional details about the verification outcome
   */
  public static VerificationResult<InclusionProofVerificationStatus> verify(RootTrustBase trustBase,
                                                                            PredicateVerifierService predicateVerifier, InclusionProof inclusionProof,
                                                                            Transaction transaction) {
    CertificationData certificationData = inclusionProof.getCertificationData().orElse(null);
    // The reference time comes from the proof, which is the only party that can state it; the
    // leaf value binds this exact value, so the SMT path below authenticates it.
    Long referenceTimeOrNull = inclusionProof.getReferenceTime().orElse(null);
    InclusionCertificate inclusionCertificate = inclusionProof.getInclusionCertificate();

    // A proof reporting no leaf at all is the aggregator's "not certified yet", and the only
    // status callers poll through.
    if (certificationData == null && referenceTimeOrNull == null && inclusionCertificate == null) {
      return new VerificationResult<>(
              "InclusionProofVerificationRule",
              InclusionProofVerificationStatus.INCLUSION_CERTIFICATE_MISSING
      );
    }

    // Anything in between establishes neither a leaf nor its absence. InclusionProof.fromCbor
    // rejects such a proof outright, so this is reachable only from one built by hand — a
    // non-conforming service behind a custom client, or a stripping proxy. Each case names what
    // is missing: folding them into the pending status would leave the caller polling to its own
    // deadline and blaming the timeout.
    if (certificationData == null) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.MISSING_CERTIFICATION_DATA);
    }

    if (referenceTimeOrNull == null) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.MISSING_REFERENCE_TIME);
    }

    if (inclusionCertificate == null) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.INCOMPLETE_INCLUSION_PROOF);
    }

    long referenceTime = referenceTimeOrNull;

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
    //
    // Both sides are Unix seconds, and both are consensus time rather than any caller's clock:
    // the reference time is the round's own timestamp from the BFT seal, so a deadline set from a
    // local clock is compared against the root chain's and the two can differ by seconds.
    //
    // Compared unsigned. The wire carries these as CBOR unsigned integers, and one at or above
    // 2^63 arrives here as a negative long with the same bit pattern (see
    // CborDeserializer.CborUnsignedLong.asLong). A signed comparison would read such a reference
    // time as less than every deadline and wave an expired request straight through, while the
    // leaf value — which is computed from the same bits — still verifies.
    if (transaction.getExpiresAt().isPresent()
            && Long.compareUnsigned(referenceTime, transaction.getExpiresAt().get()) >= 0) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.REQUEST_EXPIRED);
    }

    // A leaf cannot postdate the round that certified it. Consensus signs the round's timestamp,
    // which is that round's own reference time, so this is a free signed upper bound; the tree is
    // append-only, so a proof re-fetched later is certified by a later round and the bound only
    // loosens.
    //
    // It bounds the reference time in one direction only, and the useful direction is the other
    // one. Nothing here establishes when the leaf was actually created: a service that receives a
    // request after its deadline T can insert the leaf now and write referenceTime = T - 1 into
    // it, and both that value and this round's later timestamp satisfy every check in this rule.
    // Enforcing a deadline against a dishonest service needs signed evidence of the creation
    // round, which an inclusion proof does not carry. What this rule can establish is that the
    // leaf is internally consistent and that an honest service admitted the request in time.
    // Unsigned, for the same reason as the deadline comparison above.
    if (Long.compareUnsigned(
            referenceTime,
            inclusionProof.getUnicityCertificate().getInputRecord().getTimestamp()) > 0) {
      return new VerificationResult<>("InclusionProofVerificationRule",
              InclusionProofVerificationStatus.REFERENCE_TIME_AFTER_ROUND);
    }

    StateId stateId = StateId.fromTransaction(transaction);
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
