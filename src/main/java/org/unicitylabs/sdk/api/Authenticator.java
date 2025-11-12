package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Authenticator for transaction submission.
 */
public class Authenticator {

  private final String algorithm;
  private final Signature signature;
  private final DataHash stateHash;
  private final byte[] publicKey;

  @JsonCreator
  private Authenticator(
      @JsonProperty("algorithm") String algorithm,
      @JsonProperty("publicKey") byte[] publicKey,
      @JsonProperty("signature") Signature signature,
      @JsonProperty("stateHash") DataHash stateHash
  ) {
    this.algorithm = algorithm;
    this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
    this.signature = signature;
    this.stateHash = stateHash;
  }

  /**
   * Create authenticator from signing service.
   *
   * @param signingService  signing service
   * @param transactionHash transaction hash
   * @param stateHash       state hash
   * @return authenticator
   */
  public static Authenticator create(
      SigningService signingService,
      DataHash transactionHash,
      DataHash stateHash
  ) {
    return new Authenticator(
        signingService.getAlgorithm(),
        signingService.getPublicKey(),
        signingService.sign(transactionHash),
        stateHash
    );
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
   * Get algorithm.
   *
   * @return algorithm
   */
  public String getAlgorithm() {
    return this.algorithm;
  }

  /**
   * Get state hash.
   *
   * @return state hash
   */
  public DataHash getStateHash() {
    return this.stateHash;
  }

  /**
   * Get public key.
   *
   * @return public key
   */
  public byte[] getPublicKey() {
    return Arrays.copyOf(this.publicKey, this.publicKey.length);
  }

  /**
   * Verify if signature and data are correct.
   *
   * @param hash data hash
   * @return true if successful
   */
  public boolean verify(DataHash hash) {
    return SigningService.verifyWithPublicKey(hash, this.signature.getBytes(), this.publicKey);
  }

  /**
   * Create authenticator from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return authenticator
   */
  public static Authenticator fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new Authenticator(
        CborDeserializer.readTextString(data.get(0)),
        CborDeserializer.readByteString(data.get(1)),
        Signature.decode(CborDeserializer.readByteString(data.get(2))),
        DataHash.fromCbor(data.get(3))
    );
  }

  /**
   * Convert authenticator to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeTextString(this.algorithm),
        CborSerializer.encodeByteString(this.publicKey),
        CborSerializer.encodeByteString(this.signature.encode()),
        this.stateHash.toCbor()
    );
  }

  /**
   * Create authenticator from JSON string.
   *
   * @param input JSON string
   * @return authenticator
   */
  public static Authenticator fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, Authenticator.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Authenticator.class, e);
    }
  }

  /**
   * Convert authenticator to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Authenticator.class, e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Authenticator)) {
      return false;
    }
    Authenticator that = (Authenticator) o;
    return Objects.equals(this.algorithm, that.algorithm)
        && Objects.equals(this.signature, that.signature)
        && Objects.equals(this.stateHash, that.stateHash)
        && Objects.deepEquals(this.publicKey, that.publicKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.algorithm, this.signature, this.stateHash,
        Arrays.hashCode(this.publicKey));
  }

  @Override
  public String toString() {
    return String.format("Authenticator{algorithm=%s, signature=%s, stateHash=%s, publicKey=%s}",
        this.algorithm, this.signature, this.stateHash, HexConverter.encode(this.publicKey));
  }
}