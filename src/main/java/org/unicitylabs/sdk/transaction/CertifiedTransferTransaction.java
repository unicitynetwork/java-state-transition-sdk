package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.InclusionProof;
import org.unicitylabs.sdk.api.bft.RootTrustBase;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.transaction.verification.InclusionProofVerificationRule;
import org.unicitylabs.sdk.transaction.verification.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.util.verification.VerificationException;
import org.unicitylabs.sdk.util.verification.VerificationResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Transfer transaction with a verified inclusion proof.
 */
public class CertifiedTransferTransaction implements Transaction {

  private final TransferTransaction transaction;
  private final long referenceTime;
  private final InclusionProof inclusionProof;

  private CertifiedTransferTransaction(
          TransferTransaction transaction,
          long referenceTime,
          InclusionProof inclusionProof
  ) {
    this.transaction = transaction;
    this.referenceTime = referenceTime;
    this.inclusionProof = inclusionProof;
  }

  @Override
  public Optional<byte[]> getData() {
    return this.transaction.getData();
  }

  @Override
  public EncodedPredicate getLockScript() {
    return this.transaction.getLockScript();
  }

  @Override
  public EncodedPredicate getRecipient() {
    return this.transaction.getRecipient();
  }

  @Override
  public DataHash getSourceStateHash() {
    return this.transaction.getSourceStateHash();
  }

  @Override
  public StateMask getStateMask() {
    return this.transaction.getStateMask();
  }

  /**
   * Get inclusion proof for this transaction.
   *
   * @return inclusion proof
   */
  public InclusionProof getInclusionProof() {
    return this.inclusionProof;
  }

  /**
   * Get the reference time this transition was validated under.
   *
   * @return reference time
   */
  public long getReferenceTime() {
    return this.referenceTime;
  }

  /**
   * Deserialize a certified transfer transaction from CBOR bytes.
   *
   * @param bytes CBOR encoded certified transfer transaction
   * @param token token providing the source state for the deserialized transfer
   *
   * @return certified transfer transaction
   */
  public static CertifiedTransferTransaction fromCbor(byte[] bytes, Token token) {
    List<byte[]> data = CborDeserializer.decodeArray(bytes, 3);

    return new CertifiedTransferTransaction(
            TransferTransaction.fromCbor(data.get(0), token),
            CborDeserializer.decodeUnsignedInteger(data.get(1)).asLong(),
            InclusionProof.fromCbor(data.get(2))
    );
  }

  /**
   * Create a certified transfer transaction from a transfer transaction and inclusion proof.
   *
   * <p>The inclusion proof is verified against the transaction before creating the certified
   * instance.
   *
   * @param trustBase trust base used for proof verification
   * @param predicateVerifier predicate verifier used by verification rules
   * @param transaction transfer transaction
   * @param inclusionProof inclusion proof
   *
   * @return certified transfer transaction
   *
   * @throws VerificationException if inclusion proof verification fails
   */
  public static CertifiedTransferTransaction fromTransaction(
          RootTrustBase trustBase,
          PredicateVerifierService predicateVerifier,
          TransferTransaction transaction,
          InclusionProof inclusionProof
  ) {
    Objects.requireNonNull(trustBase, "trustBase cannot be null");
    Objects.requireNonNull(predicateVerifier, "predicateVerifier cannot be null");
    Objects.requireNonNull(transaction, "transaction cannot be null");
    Objects.requireNonNull(inclusionProof, "inclusionProof cannot be null");

    // The reference time is fixed here, at the moment the transaction is bound to its first
    // proof. Later verifiers use the carried value: a proof fetched later may be issued
    // against a later root and would then carry a different input record time.
    long referenceTime = inclusionProof.getReferenceTime()
            .orElseThrow(() -> new VerificationException(
                    "Inclusion proof verification failed",
                    new VerificationResult<>("InclusionProofVerificationRule",
                            InclusionProofVerificationStatus.MISSING_REFERENCE_TIME)));

    VerificationResult<InclusionProofVerificationStatus> result = InclusionProofVerificationRule.verify(
            trustBase,
            predicateVerifier,
            inclusionProof,
            transaction,
            referenceTime
    );
    if (result.getStatus() != InclusionProofVerificationStatus.OK) {
      throw new VerificationException("Inclusion proof verification failed", result);
    }

    return new CertifiedTransferTransaction(transaction, referenceTime, inclusionProof);
  }

  /**
   * Calculate state hash of the transfer transaction.
   *
   * @return state hash
   */
  @Override
  public DataHash calculateStateHash() {
    return this.transaction.calculateStateHash();
  }

  /**
   * Calculate hash of the transfer transaction.
   *
   * @return transaction hash
   */
  @Override
  public DataHash calculateTransactionHash() {
    return this.transaction.calculateTransactionHash();
  }

  /**
   * Serialize this certified transfer transaction to CBOR bytes.
   *
   * @return CBOR bytes
   */
  @Override
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
            this.transaction.toCbor(),
            CborSerializer.encodeUnsignedInteger(this.referenceTime),
            this.inclusionProof.toCbor());
  }

  @Override
  public String toString() {
    return String.format("CertifiedTransferTransaction{transaction=%s, referenceTime=%s, inclusionProof=%s}",
            this.transaction, this.referenceTime, this.inclusionProof);
  }
}
