package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Represents a unique request identifier derived from a public key and state hash.
 */
@JsonDeserialize(using = RequestIdJson.Deserializer.class)
public class RequestId extends DataHash {

  /**
   * Constructs a RequestId instance.
   *
   * @param hash The DataHash representing the request ID.
   */
  protected RequestId(DataHash hash) {
    super(hash.getAlgorithm(), hash.getData());
  }

  /**
   * Creates a RequestId from public key and state.
   *
   * @param publicKey public key as a byte array.
   * @param state token state.
   * @return request id
   */
  public static RequestId create(byte[] publicKey, TokenState state) {
    return RequestId.create(publicKey, state.calculateHash());
  }

  /**
   * Creates a RequestId from public key and hash.
   *
   * @param publicKey public key as a byte array.
   * @param hash hash.
   * @return request id
   */
  public static RequestId create(byte[] publicKey, DataHash hash) {
    return RequestId.create(publicKey, hash.getImprint());
  }

  /**
   * Creates a RequestId from identifier bytes and hash imprint.
   *
   * @param id id bytes.
   * @param stateBytes state bytes.
   * @return request id.
   */
  public static RequestId create(byte[] id, byte[] stateBytes) {
    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(id);
    hasher.update(stateBytes);

    return new RequestId(hasher.digest());
  }

  /**
   * Create a request id from JSON string.
   *
   * @param input JSON string
   * @return request id
   */
  public static RequestId fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, RequestId.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(RequestId.class, e);
    }
  }

  /**
   * Converts the request id to a JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(RequestId.class, e);
    }
  }

  /**
   * Converts the RequestId to a BitString.
   *
   * @return The BitString representation of the RequestId.
   */
  public BitString toBitString() {
    return BitString.fromDataHash(this);
  }

  /**
   * Returns a string representation of the RequestId.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return String.format("RequestId[%s]", HexConverter.encode(this.getImprint()));
  }
}