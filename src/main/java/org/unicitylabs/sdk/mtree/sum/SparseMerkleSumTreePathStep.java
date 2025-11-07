package org.unicitylabs.sdk.mtree.sum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.BigIntegerAsStringSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Step in a sparse merkle sum tree path.
 */
public class SparseMerkleSumTreePathStep {

  private final BigInteger path;
  private final byte[] data;
  private final BigInteger value;

  @JsonCreator
  SparseMerkleSumTreePathStep(
      @JsonProperty("path") BigInteger path,
      @JsonProperty("data") byte[] data,
      @JsonProperty("value") BigInteger value
  ) {
    Objects.requireNonNull(path, "path cannot be null");
    Objects.requireNonNull(value, "value cannot be null");

    this.path = path;
    this.data = data;
    this.value = value;
  }

  /**
   * Get path of the step.
   *
   * @return path
   */
  @JsonSerialize(using = BigIntegerAsStringSerializer.class)
  public BigInteger getPath() {
    return this.path;
  }

  /**
   * Get data of step.
   *
   * @return data
   */
  public Optional<byte[]> getData() {
    return Optional.ofNullable(this.data);
  }

  /**
   * Get value of step.
   *
   * @return value
   */
  @JsonSerialize(using = BigIntegerAsStringSerializer.class)
  public BigInteger getValue() {
    return this.value;
  }

  /**
   * Create a step from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return step
   */
  public static SparseMerkleSumTreePathStep fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SparseMerkleSumTreePathStep(
        BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(0))),
        CborDeserializer.readOptional(data.get(1), CborDeserializer::readByteString),
        BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(2)))
    );
  }

  /**
   * Convert step to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeByteString(BigIntegerConverter.encode(this.path)),
        CborSerializer.encodeOptional(this.data, CborSerializer::encodeByteString),
        CborSerializer.encodeByteString(BigIntegerConverter.encode(this.value))
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleSumTreePathStep)) {
      return false;
    }
    SparseMerkleSumTreePathStep that = (SparseMerkleSumTreePathStep) o;
    return Objects.equals(this.path, that.path) && Arrays.equals(this.data, that.data)
        && Objects.equals(this.value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, Arrays.hashCode(this.data), this.value);
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePathStep{path=%s, data=%s, value=%s}",
        this.path.toString(2),
        this.data == null ? null : HexConverter.encode(this.data),
        this.value
    );
  }
}
