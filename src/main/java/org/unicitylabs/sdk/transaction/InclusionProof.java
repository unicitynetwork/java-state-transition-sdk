package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
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
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

/**
 * Represents a proof of inclusion or non-inclusion in a sparse merkle tree.
 */
public class InclusionProof {

  private final SparseMerkleTreePath merkleTreePath;
  private final Authenticator authenticator;
  private final DataHash transactionHash;
  private final UnicityCertificate unicityCertificate;

  @JsonCreator
  InclusionProof(
      @JsonProperty("merkleTreePath") SparseMerkleTreePath merkleTreePath,
      @JsonProperty("authenticator") Authenticator authenticator,
      @JsonProperty("transactionHash") DataHash transactionHash,
      @JsonProperty("unicityCertificate") UnicityCertificate unicityCertificate
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

  /**
   * Get merkle tree path.
   *
   * @return merkle tree path
   */
  public SparseMerkleTreePath getMerkleTreePath() {
    return this.merkleTreePath;
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
   * Get authenticator on inclusion proof, null on non inclusion proof.
   *
   * @return authenticator
   */
  public Optional<Authenticator> getAuthenticator() {
    return Optional.ofNullable(this.authenticator);
  }

  /**
   * Get authenticator on inclusion proof, null on non inclusion proof.
   *
   * @return inclusion proof
   */
  public Optional<DataHash> getTransactionHash() {
    return Optional.ofNullable(this.transactionHash);
  }

  /**
   * Verify inclusion proof.
   *
   * @param requestId request id
   * @param trustBase trust base for unicity certificate anchor verification
   * @return inclusion proof verification status
   */
  public InclusionProofVerificationStatus verify(RequestId requestId, RootTrustBase trustBase) {
    // Check if path is valid and signed by a trusted authority
    if (!new UnicityCertificateVerificationRule().verify(
        new UnicityCertificateVerificationContext(
            this.merkleTreePath.getRootHash(),
            this.unicityCertificate,
            trustBase
        )
    ).isSuccessful()) {
      return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
    }

    MerkleTreePathVerificationResult result = this.merkleTreePath.verify(
        requestId.toBitString().toBigInteger());
    if (!result.isPathValid()) {
      return InclusionProofVerificationStatus.PATH_INVALID;
    }

    if (this.authenticator != null && this.transactionHash != null) {
      if (!this.authenticator.verify(this.transactionHash)) {
        return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
      }

      try {
        LeafValue leafValue = LeafValue.create(this.authenticator, this.transactionHash);
        if (this.merkleTreePath.getSteps().size() == 0) {
          return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
        }

        SparseMerkleTreePathStep step = this.merkleTreePath.getSteps().get(0);
        if (!Arrays.equals(leafValue.getBytes(), step.getData().orElse(null))) {
          return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
        }
      } catch (CborSerializationException e) {
        return InclusionProofVerificationStatus.NOT_AUTHENTICATED;
      }
    }

    if (!result.isPathIncluded()) {
      return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
    }

    return InclusionProofVerificationStatus.OK;
  }

  /**
   * Create inclusion proof from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return inclusion proof
   */
  public static InclusionProof fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new InclusionProof(
        SparseMerkleTreePath.fromCbor(data.get(0)),
        CborDeserializer.readOptional(data.get(1), Authenticator::fromCbor),
        CborDeserializer.readOptional(data.get(2), DataHash::fromCbor),
        UnicityCertificate.fromCbor(data.get(3))
    );
  }

  /**
   * Convert inclusion proof to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        this.merkleTreePath.toCbor(),
        CborSerializer.encodeOptional(this.authenticator, Authenticator::toCbor),
        CborSerializer.encodeOptional(this.transactionHash, DataHash::toCbor),
        this.unicityCertificate.toCbor()
    );
  }

  /**
   * Get inclusion proof from JSON.
   *
   * @param input inclusion proof JSON string
   * @return inclusion proof
   */
  public static InclusionProof fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, InclusionProof.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProof.class, e);
    }
  }

  /**
   * Get inclusion proof as JSON.
   *
   * @return inclusion proof JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProof.class, e);
    }
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
