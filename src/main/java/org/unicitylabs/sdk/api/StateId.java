package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Represents a unique state identifier derived from a public key and state hash.
 */
@JsonDeserialize(using = StateIdJson.Deserializer.class)
public class StateId extends DataHash {

  /**
   * Constructs a StateId instance.
   *
   * @param hash The DataHash representing the state ID.
   */
  protected StateId(DataHash hash) {
    super(hash.getAlgorithm(), hash.getData());
  }

  /**
   * Creates a StateId from public key and state.
   *
   * @param publicKey public key as a byte array.
   * @param state     token state.
   * @return state id
   */
  public static StateId create(byte[] publicKey, TokenState state) {
    return StateId.create(publicKey, state.calculateHash());
  }

  /**
   * Creates a StateId from public key and hash.
   *
   * @param publicKey public key as a byte array.
   * @param hash      hash.
   * @return state id
   */
  public static StateId create(byte[] publicKey, DataHash hash) {
    return StateId.create(publicKey, hash.getImprint());
  }

  /**
   * Creates a StateId from identifier bytes and hash imprint.
   *
   * @param publicKey   public key bytes.
   * @param hashImprint state bytes.
   * @return state id.
   */
  public static StateId create(byte[] publicKey, byte[] hashImprint) {
    return new StateId(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(hashImprint),
                    CborSerializer.encodeByteString(publicKey)
                )
            )
            .digest()
    );
  }

  /**
   * Create a state id from JSON string.
   *
   * @param input JSON string
   * @return state id
   */
  public static StateId fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, StateId.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(StateId.class, e);
    }
  }

  /**
   * Converts the state id to a JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(StateId.class, e);
    }
  }

  /**
   * Converts the StateId to a BitString.
   *
   * @return The BitString representation of the StateId.
   */
  public BitString toBitString() {
    return BitString.fromDataHash(this);
  }

  /**
   * Returns a string representation of the StateId.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return String.format("StateId[%s]", HexConverter.encode(this.getImprint()));
  }
}