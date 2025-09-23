package org.unicitylabs.sdk.transaction;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.LeafValue;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationContext;
import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationRule;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.mtree.MerkleTreePathVerificationResult;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;

/**
 * Represents a proof of inclusion or non-inclusion in a sparse merkle tree.
 */
public class InclusionProof {

  private final SparseMerkleTreePath merkleTreePath;
  private final Authenticator authenticator;
  private final DataHash transactionHash;
  private final UnicityCertificate unicityCertificate;

  public InclusionProof(
      SparseMerkleTreePath merkleTreePath,
      Authenticator authenticator,
      DataHash transactionHash,
      UnicityCertificate unicityCertificate
  ) {
    Objects.requireNonNull(merkleTreePath, "Merkle tree path cannot be null.");
    Objects.requireNonNull(unicityCertificate, "Unicity certificate cannot be null.");

    if ((authenticator == null) != (transactionHash == null)) {
      throw new IllegalArgumentException(
          "Authenticator and transaction hash must be both set or both null.");
    }
    this.merkleTreePath = merkleTreePath;
    this.authenticator = authenticator;
    this.transactionHash = transactionHash;
    this.unicityCertificate = unicityCertificate;
  }

  public SparseMerkleTreePath getMerkleTreePath() {
    return this.merkleTreePath;
  }

  public UnicityCertificate getUnicityCertificate() {
    return this.unicityCertificate;
  }

  public Optional<Authenticator> getAuthenticator() {
    return Optional.ofNullable(this.authenticator);
  }

  public Optional<DataHash> getTransactionHash() {
    return Optional.ofNullable(this.transactionHash);
  }

  public InclusionProofVerificationStatus verify(RequestId requestId) {
    if (this.authenticator != null && this.transactionHash != null) {
      if (!this.authenticator.verify(this.transactionHash)) {
        return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
      }

      try {
        LeafValue leafValue = LeafValue.create(this.authenticator, this.transactionHash);
        SparseMerkleTreePathStep step = this.merkleTreePath.getSteps().get(0);
        if (step == null || !Arrays.equals(leafValue.getBytes(), step.getBranch().map(
            SparseMerkleTreePathStep.Branch::getValue).orElse(null))) {
          return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
        }
      } catch (CborSerializationException e) {
        return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
      }
    }

//    TODO: Fix Unicity certificate verification
//    if (!new UnicityCertificateVerificationRule().verify(
//        new UnicityCertificateVerificationContext(
//            this.merkleTreePath.getRootHash(),
//            this.unicityCertificate,
//            null
//        )
//    ).isSuccessful()) {
//      return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
//    }

    MerkleTreePathVerificationResult result = this.merkleTreePath.verify(
        requestId.toBitString().toBigInteger());
    if (!result.isPathValid()) {
      return InclusionProofVerificationStatus.PATH_INVALID;
    }

    if (!result.isPathIncluded()) {
      return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
    }

    return InclusionProofVerificationStatus.OK;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InclusionProof)) {
      return false;
    }
    InclusionProof that = (InclusionProof) o;
    return Objects.equals(merkleTreePath, that.merkleTreePath) && Objects.equals(authenticator,
        that.authenticator) && Objects.equals(transactionHash, that.transactionHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(merkleTreePath, authenticator, transactionHash);
  }

  @Override
  public String toString() {
    return String.format("InclusionProof{merkleTreePath=%s, authenticator=%s, transactionHash=%s}",
        merkleTreePath, authenticator, transactionHash);
  }
}
