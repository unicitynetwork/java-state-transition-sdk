package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.api.bft.UnicityCertificate;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Represents a proof of inclusion or non-inclusion in a sparse merkle tree.
 */
public class InclusionProof {
  public static final long CBOR_TAG = 39033;
  private static final int VERSION = 1;

  private final InclusionCertificate inclusionCertificate;
  private final CertificationData certificationData;
  private final Long referenceTime;
  private final UnicityCertificate unicityCertificate;

  InclusionProof(
          CertificationData certificationData,
          Long referenceTime,
          InclusionCertificate inclusionCertificate,
          UnicityCertificate unicityCertificate
  ) {
    Objects.requireNonNull(unicityCertificate, "Unicity certificate cannot be null.");

    this.inclusionCertificate = inclusionCertificate;
    this.certificationData = certificationData;
    this.referenceTime = referenceTime;
    this.unicityCertificate = unicityCertificate;
  }

  public int getVersion() {
    return VERSION;
  }

  /**
   * Get merkle tree path.
   *
   * @return merkle tree path
   */
  public InclusionCertificate getInclusionCertificate() {
    return this.inclusionCertificate;
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
   * Get certification data on inclusion proof, null on non inclusion proof.
   *
   * @return authenticator
   */
  public Optional<CertificationData> getCertificationData() {
    return Optional.ofNullable(this.certificationData);
  }

  /**
   * Get the reference time of the round the certified leaf was created in, empty on a
   * non-inclusion proof.
   *
   * <p>It cannot be recovered from the certificate chain: an aggregator serves proofs against
   * the current certified root, whose input record time is that of the latest round rather
   * than the one the leaf was created under.
   *
   * @return reference time
   */
  public Optional<Long> getReferenceTime() {
    return Optional.ofNullable(this.referenceTime);
  }

  /**
   * Deserialize inclusion proof from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return inclusion proof
   */
  public static InclusionProof fromCbor(byte[] bytes) {
    CborDeserializer.CborTag tag = CborDeserializer.decodeTag(bytes);
    if (tag.getTag() != InclusionProof.CBOR_TAG) {
      throw new CborSerializationException(String.format("Invalid CBOR tag: %s", tag.getTag()));
    }
    List<byte[]> data = CborDeserializer.decodeArray(tag.getData(), 5);

    int version = CborDeserializer.decodeUnsignedInteger(data.get(0)).asInt();
    if (version != VERSION) {
      throw new CborSerializationException(String.format("Unsupported version: %s", version));
    }

    CertificationData certificationData =
            CborDeserializer.decodeNullable(data.get(1), CertificationData::fromCbor);
    Long referenceTime = CborDeserializer.decodeNullable(data.get(2), value ->
            CborDeserializer.decodeUnsignedInteger(value).asLong());
    InclusionCertificate inclusionCertificate =
            CborDeserializer.decodeNullable(data.get(3), (certificate) ->
                    InclusionCertificate.decode(CborDeserializer.decodeByteString(certificate)));

    // A proof either establishes a leaf or reports that there is none yet. A partially present
    // proof is neither, and would let a caller reach a leaf check with a reference time nothing
    // certified.
    long present = Stream.of(certificationData, referenceTime, inclusionCertificate)
            .filter(Objects::nonNull)
            .count();
    if (present != 0 && present != 3) {
      throw new CborSerializationException(
              "InclusionProof must carry certification data, reference time and inclusion "
                      + "certificate together, or none of them.");
    }

    return new InclusionProof(
            certificationData,
            referenceTime,
            inclusionCertificate,
            UnicityCertificate.fromCbor(data.get(4))
    );
  }

  /**
   * Serialize inclusion proof to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    byte[] payload = CborSerializer.encodeArray(CborSerializer.encodeUnsignedInteger(VERSION),
            CborSerializer.encodeNullable(this.certificationData, CertificationData::toCbor),
            CborSerializer.encodeNullable(this.referenceTime,
                    CborSerializer::encodeUnsignedInteger),
            CborSerializer.encodeNullable(this.inclusionCertificate, certificate ->
                    CborSerializer.encodeByteString(certificate.encode())), this.unicityCertificate.toCbor());
    return CborSerializer.encodeTag(
            InclusionProof.CBOR_TAG,
            payload
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof InclusionProof)) {
      return false;
    }
    InclusionProof that = (InclusionProof) o;
    return Objects.equals(this.inclusionCertificate, that.inclusionCertificate) && Objects.equals(this.certificationData, that.certificationData) && Objects.equals(this.referenceTime, that.referenceTime) && Objects.equals(this.unicityCertificate, that.unicityCertificate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.inclusionCertificate, this.certificationData, this.referenceTime, this.unicityCertificate);
  }

  @Override
  public String toString() {
    return String.format(
            "InclusionProof{certificationData=%s, referenceTime=%s, inclusionCertificate=%s, unicityCertificate=%s}",
            this.certificationData,
            this.referenceTime,
            this.inclusionCertificate,
            this.unicityCertificate
    );
  }
}
