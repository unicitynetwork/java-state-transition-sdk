package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.api.bft.UnicityCertificate;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

import java.util.List;

/**
 * Inclusion proof response.
 */
public class InclusionProofResponse {

  private final long blockNumber;
  private final InclusionProof inclusionProof;
  private final UnicityCertificate unicityCertificate;

  /**
   * Create inclison proof response.
   *
   * @param inclusionProof inclusion proof
   */
  InclusionProofResponse(
          long blockNumber,
          InclusionProof inclusionProof,
          UnicityCertificate unicityCertificate
  ) {
    this.blockNumber = blockNumber;
    this.inclusionProof = inclusionProof;
    this.unicityCertificate = unicityCertificate;
  }

  /**
   * Get the certificate of the round this answer was served against. Present either way.
   *
   * @return unicity certificate
   */
  public UnicityCertificate getUnicityCertificate() {
    return this.unicityCertificate;
  }

  /**
   * Get inclusion proof.
   *
   * @return inclusion proof
   */
  /**
   * Get the certified leaf, or null when the state is not certified yet.
   *
   * @return inclusion proof, or null
   */
  public InclusionProof getInclusionProof() {
    return this.inclusionProof;
  }

  /**
   * Deserialize response from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return inclusion proof response
   */
  public static InclusionProofResponse fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.decodeArray(bytes, 2);
    return new InclusionProofResponse(
            CborDeserializer.decodeUnsignedInteger(data.get(0)).asLong(),
            InclusionProof.decodeOrAbsent(data.get(1)),
            unicityCertificateOf(data.get(1))
    );
  }

  /**
   * Serialize inclusion proof response to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(this.blockNumber),
            this.inclusionProof == null
                    ? InclusionProof.encodeNoCertifiedLeaf(this.unicityCertificate)
                    : this.inclusionProof.toCbor()
    );
  }

  /**
   * Read the unicity certificate out of the wire form, which carries it either way.
   *
   * @param bytes encoded inclusion proof
   * @return unicity certificate
   */
  private static UnicityCertificate unicityCertificateOf(byte[] bytes) {
    return UnicityCertificate.fromCbor(
            CborDeserializer.decodeArray(
                    CborDeserializer.decodeTag(bytes).getData(), 5).get(4));
  }
}
