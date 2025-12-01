package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;

/**
 * Submit commitment response.
 */
public class CertificationResponse {

  private final CertificationStatus status;
  private final Receipt receipt;

  /**
   * Create submit commitment response.
   *
   * @param status  status
   * @param receipt receipt
   */
  @JsonCreator
  CertificationResponse(
      @JsonProperty("status") CertificationStatus status,
      @JsonProperty("receipt") Receipt receipt
  ) {
    this.status = status;
    this.receipt = receipt;
  }

  /**
   * Get status.
   *
   * @return status
   */
  public CertificationStatus getStatus() {
    return this.status;
  }

  /**
   * Get receipt.
   *
   * @return receipt
   */
  public Optional<Receipt> getReceipt() {
    return Optional.ofNullable(this.receipt);
  }

  /**
   * Create a new certification response.
   *
   * @param signingService    Aggregator signing service
   * @param certificationData Certification data
   * @param status            Certification response status
   * @return Created certification response
   */
  public static CertificationResponse createWithReceipt(
      SigningService signingService,
      CertificationData certificationData,
      CertificationStatus status
  ) {
    Signature signature = signingService.sign(
        new DataHasher(HashAlgorithm.SHA256).update(certificationData.toCbor()).digest()
    );

    return new CertificationResponse(status, new Receipt(signingService.getPublicKey(), signature));
  }

  /**
   * Create a new certification response.
   *
   * @param status Certification response status
   * @return certification response
   */
  public static CertificationResponse create(CertificationStatus status) {
    return new CertificationResponse(status, null);
  }

  /**
   * Create submit commitment response from JSON string.
   *
   * @param input JSON string
   * @return submit commitment response
   */
  public static CertificationResponse fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, CertificationResponse.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(CertificationResponse.class, e);
    }
  }

  /**
   * Convert submit commitment response to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(CertificationResponse.class, e);
    }
  }

  @Override
  public String toString() {
    return String.format("CertificationResponse{status=%s}", this.status);
  }

  /**
   * Certification receipt to confirm certification.
   */
  public static class Receipt {

    private final byte[] publicKey;
    private final Signature signature;

    @JsonCreator
    private Receipt(
        @JsonProperty("publicKey") byte[] publicKey,
        @JsonProperty("signature") Signature signature
    ) {
      this.publicKey = publicKey;
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
     * Get signature.
     *
     * @return signature
     */
    public Signature getSignature() {
      return this.signature;
    }

    /**
     * Verify receipt for certification data.
     *
     * @param certificationData certification data
     * @return true if receipt is valid
     */
    public boolean verify(CertificationData certificationData) {
      return SigningService.verifyWithPublicKey(
          certificationData.calculateLeafValue(),
          this.signature.getBytes(),
          this.publicKey
      );
    }

    /**
     * Serialize receipt to CBOR bytes.
     *
     * @return CBOR bytes
     */
    public byte[] toCbor() {
      return CborSerializer.encodeArray(CborSerializer.encodeByteString(this.publicKey), this.signature.toCbor());
    }

    /**
     * Create receipt from CBOR bytes.
     *
     * @param bytes CBOR bytes
     * @return receipt
     */
    public static Receipt fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);
      return new Receipt(
          CborDeserializer.readByteString(data.get(0)),
          Signature.fromCbor(data.get(1))
      );
    }
  }
}
