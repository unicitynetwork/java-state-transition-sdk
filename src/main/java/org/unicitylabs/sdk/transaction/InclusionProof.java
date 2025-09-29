package org.unicitylabs.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
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
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep.Branch;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.util.BigIntegerConverter;

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

  @JsonGetter("merkleTreePath")
  public SparseMerkleTreePath getMerkleTreePath() {
    return this.merkleTreePath;
  }

  @JsonGetter("unicityCertificate")
  public UnicityCertificate getUnicityCertificate() {
    return this.unicityCertificate;
  }

  @JsonGetter("authenticator")
  public Optional<Authenticator> getAuthenticator() {
    return Optional.ofNullable(this.authenticator);
  }

  @JsonGetter("transactionHash")
  public Optional<DataHash> getTransactionHash() {
    return Optional.ofNullable(this.transactionHash);
  }

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
        SparseMerkleTreePathStep step = this.merkleTreePath.getSteps().get(0);
        if (step == null || !Arrays.equals(leafValue.getBytes(), step.getBranch().map(
            SparseMerkleTreePathStep.Branch::getValue).orElse(null))) {
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

  public static InclusionProof fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new InclusionProof(
        SparseMerkleTreePath.fromCbor(data.get(0)),
        CborDeserializer.readOptional(data.get(1), Authenticator::fromCbor),
        CborDeserializer.readOptional(data.get(2), DataHash::fromCbor),
        UnicityCertificate.fromCbor(data.get(3))
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        this.merkleTreePath.toCbor(),
        CborSerializer.encodeOptional(this.authenticator, Authenticator::toCbor),
        CborSerializer.encodeOptional(this.transactionHash, DataHash::toCbor),
        this.unicityCertificate.toCbor()
    );
  }

  public static InclusionProof fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, InclusionProof.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(InclusionProof.class, e);
    }
  }

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
