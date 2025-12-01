package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Certification data.
 */
public class CertificationData {

  private final byte[] publicKey;
  private final DataHash sourceStateHash;
  private final DataHash transactionHash;
  private final Signature signature;

  /**
   * Certification data.
   *
   * @param publicKey       public key.
   * @param sourceStateHash source state hash.
   * @param transactionHash transaction hash.
   * @param signature       signature.
   */
  @JsonCreator
  private CertificationData(
      @JsonProperty("publicKey") byte[] publicKey,
      @JsonProperty("sourceStateHash") DataHash sourceStateHash,
      @JsonProperty("transactionHash") DataHash transactionHash,
      @JsonProperty("signature") Signature signature
  ) {
    this.publicKey = publicKey;
    this.sourceStateHash = sourceStateHash;
    this.transactionHash = transactionHash;
    this.signature = signature;
  }

  /**
   * Get copy of public key.
   *
   * @return public key
   */
  public byte[] getPublicKey() {
    return Arrays.copyOf(this.publicKey, this.publicKey.length);
  }

  /**
   * Get source state hash.
   *
   * @return source state hash
   */
  public DataHash getSourceStateHash() {
    return this.sourceStateHash;
  }

  /**
   * Get transaction hash.
   *
   * @return transaction hash
   */
  public DataHash getTransactionHash() {
    return this.transactionHash;
  }

  /**
   * Get signature.
   *
   * @return signature
   */
  public Signature getSignature() {
    return this.signature;
  }

  /**
   * Create CertificationData from signing service.
   *
   * @param sourceStateHash source state hash
   * @param transactionHash transaction hash
   * @param signingService  signing service
   * @return CertificationData
   */
  public static CertificationData create(
      DataHash sourceStateHash,
      DataHash transactionHash,
      SigningService signingService
  ) {
    Objects.requireNonNull(sourceStateHash, "Source state hash cannot be null");
    Objects.requireNonNull(transactionHash, "Transaction hash cannot be null");

    return new CertificationData(
        signingService.getPublicKey(),
        sourceStateHash,
        transactionHash,
        signingService.sign(
            new DataHasher(HashAlgorithm.SHA256)
                .update(
                    CborSerializer.encodeArray(sourceStateHash.toCbor(), transactionHash.toCbor())
                )
                .digest()
        ));
  }


  /**
   * Create CertificationData from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return CertificationData
   */
  public static CertificationData fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new CertificationData(
        CborDeserializer.readByteString(data.get(0)),
        DataHash.fromCbor(data.get(1)),
        DataHash.fromCbor(data.get(2)),
        Signature.fromCbor(data.get(3))
    );
  }

  /**
   * Calculate state ID.
   *
   * @return state ID
   */
  public StateId calculateStateId() {
    return StateId.create(this.publicKey, this.sourceStateHash);
  }

  /**
   * Calculate leaf value for Merkle tree.
   *
   * @return leaf value
   */
  public DataHash calculateLeafValue() {
    return new DataHasher(HashAlgorithm.SHA256)
        .update(this.toCbor())
        .digest();
  }

  /**
   * Verifies current certification data.
   *
   * @return true if valid, false otherwise
   */
  public boolean verify() {
    return SigningService.verifyWithPublicKey(
        new DataHasher(HashAlgorithm.SHA256)
            .update(CborSerializer.encodeArray(this.sourceStateHash.toCbor(), this.transactionHash.toCbor()))
            .digest(),
        this.signature.getBytes(),
        this.publicKey
    );
  }

  /**
   * Convert the certification data to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeByteString(this.publicKey),
        this.sourceStateHash.toCbor(),
        this.transactionHash.toCbor(),
        this.signature.toCbor()
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CertificationData)) {
      return false;
    }
    CertificationData that = (CertificationData) o;
    return Arrays.equals(this.publicKey, that.publicKey)
        && Objects.equals(this.sourceStateHash, that.sourceStateHash)
        && Objects.equals(this.transactionHash, that.transactionHash)
        && Objects.equals(this.signature, that.signature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.publicKey), this.sourceStateHash, this.transactionHash, this.signature);
  }

  @Override
  public String toString() {
    return String.format("Commitment{publicKey=%s, sourceStateHash=%s, transactionHash=%s, signature=%s}",
        HexConverter.encode(this.publicKey), this.sourceStateHash, this.transactionHash, this.signature);
  }
}
