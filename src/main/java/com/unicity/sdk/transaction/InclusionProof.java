
package com.unicity.sdk.transaction;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.LeafValue;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;

import com.unicity.sdk.mtree.MerkleTreePathVerificationResult;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePath;
import com.unicity.sdk.mtree.plain.SparseMerkleTreePathStep;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a proof of inclusion or non-inclusion in a sparse merkle tree.
 */
public class InclusionProof {

  private final SparseMerkleTreePath merkleTreePath;
  private final Authenticator authenticator;
  private final DataHash transactionHash;

  public InclusionProof(SparseMerkleTreePath merkleTreePath, Authenticator authenticator,
      DataHash transactionHash) {
    if ((authenticator == null) != (transactionHash == null)) {
      throw new IllegalArgumentException(
          "Authenticator and transaction hash must be both set or both null.");
    }
    this.merkleTreePath = merkleTreePath;
    this.authenticator = authenticator;
    this.transactionHash = transactionHash;
  }

  public SparseMerkleTreePath getMerkleTreePath() {
    return this.merkleTreePath;
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
        if (step == null || step.getBranch() == null
            || !Arrays.equals(leafValue.getBytes(), step.getBranch().getValue())) {
          return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
        }
      } catch (IOException e) {
        return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
      }
    }

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
