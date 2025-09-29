package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
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
import org.unicitylabs.sdk.transaction.TransferTransaction.Data;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Authenticator for transaction submission
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
    this.publicKey = publicKey;
    this.signature = signature;
    this.stateHash = stateHash;
  }

  public static Authenticator create(
      SigningService signingService,
      DataHash transactionHash,
      DataHash stateHash
  ) {
    return new Authenticator(signingService.getAlgorithm(), signingService.getPublicKey(),
        signingService.sign(transactionHash), stateHash);
  }

  @JsonGetter("signature")
  public Signature getSignature() {
    return this.signature;
  }

  @JsonGetter("algorithm")
  public String getAlgorithm() {
    return this.algorithm;
  }

  @JsonGetter("stateHash")
  public DataHash getStateHash() {
    return this.stateHash;
  }

  @JsonGetter("publicKey")
  public byte[] getPublicKey() {
    return this.publicKey;
  }

  public boolean verify(DataHash hash) {
    return SigningService.verifyWithPublicKey(hash, this.signature.getBytes(), this.publicKey);
  }

  public static Authenticator fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new Authenticator(
        CborDeserializer.readTextString(data.get(0)),
        CborDeserializer.readByteString(data.get(1)),
        Signature.decode(CborDeserializer.readByteString(data.get(2))),
        DataHash.fromCbor(data.get(3))
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeTextString(this.algorithm),
        CborSerializer.encodeByteString(this.publicKey),
        CborSerializer.encodeByteString(this.signature.encode()),
        this.stateHash.toCbor()
    );
  }

  public static Authenticator fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, Authenticator.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(Authenticator.class, e);
    }
  }

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