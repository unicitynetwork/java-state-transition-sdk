
package org.unicitylabs.sdk.hash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Arrays;
import java.util.Objects;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * DataHash represents a hash of data using a specific hash algorithm.
 */
@JsonSerialize(using = DataHashJson.Serializer.class)
@JsonDeserialize(using = DataHashJson.Deserializer.class)
public class DataHash {

  private final byte[] data;
  private final HashAlgorithm algorithm;

  /**
   * Constructs a DataHash with the specified algorithm and data.
   *
   * @param algorithm The hash algorithm used to compute the hash
   * @param data      The byte array representing the hash
   * @throws IllegalArgumentException if algorithm or data is null
   */
  public DataHash(HashAlgorithm algorithm, byte[] data) {
    Objects.requireNonNull(algorithm, "algorithm cannot be null");
    Objects.requireNonNull(data, "data cannot be null");

    this.data = Arrays.copyOf(data, data.length);
    this.algorithm = algorithm;
  }

  /**
   * Creates a DataHash from an imprint (algorithm prefix + hash bytes). The imprint format is:
   * [algorithm_byte_1][algorithm_byte_2][hash_bytes...]
   *
   * @param imprint The imprint bytes containing algorithm identifier and hash
   * @return A new DataHash instance
   */
  public static DataHash fromImprint(byte[] imprint) {
    if (imprint.length < 3) {
      throw new IllegalArgumentException("Imprint too short");
    }

    // Extract algorithm from first two bytes
    int algorithmValue = ((imprint[0] & 0xFF) << 8) | (imprint[1] & 0xFF);
    HashAlgorithm algorithm = HashAlgorithm.fromValue(algorithmValue);

    // Extract hash data
    byte[] hashData = Arrays.copyOfRange(imprint, 2, imprint.length);

    return new DataHash(algorithm, hashData);
  }

  /**
   * Returns the data bytes of this DataHash.
   *
   * @return A copy of the hash data
   */
  public byte[] getData() {
    return Arrays.copyOf(this.data, this.data.length);
  }

  /**
   * Returns the hash algorithm used for this DataHash.
   *
   * @return The hash algorithm
   */
  public HashAlgorithm getAlgorithm() {
    return this.algorithm;
  }

  /**
   * Returns the imprint of this DataHash (algorithm + hash bytes). Format:
   * [algorithm_byte_1][algorithm_byte_2][hash_bytes...]
   */
  public byte[] getImprint() {
    byte[] imprint = new byte[this.data.length + 2];
    int algorithmValue = this.algorithm.getValue();
    imprint[0] = (byte) ((algorithmValue & 0xFF00) >> 8);
    imprint[1] = (byte) (algorithmValue & 0xFF);
    System.arraycopy(this.data, 0, imprint, 2, this.data.length);

    return imprint;
  }

  public static DataHash fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, DataHash.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(DataHash.class, e);
    }
  }

  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(DataHash.class, e);
    }
  }

  public static DataHash fromCbor(byte[] bytes) {
    return DataHash.fromImprint(CborDeserializer.readByteString(bytes));
  }

  public byte[] toCbor() {
    return CborSerializer.encodeByteString(this.getImprint());
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DataHash)) {
      return false;
    }
    DataHash that = (DataHash) o;
    return Arrays.equals(this.data, that.data) && this.algorithm == that.algorithm;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.algorithm, Arrays.hashCode(this.data));
  }

  @Override
  public String toString() {
    return String.format("[%s]%s", this.algorithm.name(), HexConverter.encode(this.data));
  }
}
