package org.unicitylabs.sdk.transaction;

import org.unicitylabs.sdk.api.InclusionProof;
import org.unicitylabs.sdk.api.NetworkId;
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
 * Mint transaction bundled with an inclusion proof.
 */
public class CertifiedMintTransaction implements Transaction {

  private final MintTransaction transaction;
  private final long referenceTime;
  private final InclusionProof inclusionProof;

  private CertifiedMintTransaction(MintTransaction transaction, long referenceTime,
                                   InclusionProof inclusionProof) {
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

  /**
   * Returns the network identifier.
   *
   * @return network id
   */
  public NetworkId getNetworkId() {
    return this.transaction.getNetworkId();
  }

  /**
   * Returns the mint-transaction salt.
   *
   * @return token salt
   */
  public TokenSalt getSalt() {
    return this.transaction.getSalt();
  }

  /**
   * Returns the token identifier.
   *
   * @return token id
   */
  public TokenId getTokenId() {
    return this.transaction.getTokenId();
  }

  /**
   * Returns the token type.
   *
   * @return token type
   */
  public TokenType getTokenType() {
    return this.transaction.getTokenType();
  }

  public Optional<byte[]> getJustification() {
    return this.transaction.getJustification();
  }

  @Override
  public StateMask getStateMask() {
    return this.transaction.getStateMask();
  }

  /**
   * Returns the inclusion proof certifying this transaction.
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
   * Deserializes a certified mint transaction from CBOR.
   *
   * @param bytes CBOR-encoded certified mint transaction
   * @return decoded certified mint transaction
   */
  public static CertifiedMintTransaction fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.decodeArray(bytes, 3);
    return new CertifiedMintTransaction(
            MintTransaction.fromCbor(data.get(0)),
            CborDeserializer.decodeUnsignedInteger(data.get(1)).asLong(),
            InclusionProof.fromCbor(data.get(2)));
  }

  /**
   * Creates a certified mint transaction after verifying the inclusion proof.
   *
   * @param trustBase trust base used to verify inclusion proof signatures
   * @param predicateVerifier service used for predicate verification during proof validation
   * @param transaction mint transaction to certify
   * @param inclusionProof inclusion proof for the transaction
   * @return certified mint transaction
   * @throws VerificationException if inclusion proof verification fails
   */
  public static CertifiedMintTransaction fromTransaction(
          RootTrustBase trustBase,
          PredicateVerifierService predicateVerifier,
          MintTransaction transaction,
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

    return new CertifiedMintTransaction(transaction, referenceTime, inclusionProof);
  }

  @Override
  public DataHash calculateStateHash() {
    return this.transaction.calculateStateHash();
  }

  @Override
  public DataHash calculateTransactionHash() {
    return this.transaction.calculateTransactionHash();
  }

  @Override
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
            this.transaction.toCbor(),
            CborSerializer.encodeUnsignedInteger(this.referenceTime),
            this.inclusionProof.toCbor());
  }

  @Override
  public String toString() {
    return String.format("CertifiedMintTransaction{transaction=%s, referenceTime=%s, inclusionProof=%s}",
            this.transaction, this.referenceTime, this.inclusionProof);
  }
}
