package org.unicitylabs.sdk.mtree.plain;

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
 * Sparse Merkle tree path step.
 */
public class SparseMerkleTreePathStep {

  private final BigInteger path;
  private final byte[] data;

  /**
   * Create sparse Merkle tree path step.
   *
   * @param path step path, must be greater than or equal to zero
   * @param data step data
   */
  @JsonCreator
  public SparseMerkleTreePathStep(
      @JsonProperty("path") BigInteger path,
      @JsonProperty("data") byte[] data
  ) {
    Objects.requireNonNull(path, "path cannot be null");
    if (path.compareTo(BigInteger.ZERO) < 0) {
      throw new IllegalArgumentException("path should be non negative");
    }

    this.path = path;
    this.data = data;
  }

  /**
   * Get path.
   *
   * @return step path
   */
  @JsonSerialize(using = BigIntegerAsStringSerializer.class)
  public BigInteger getPath() {
    return this.path;
  }

  /**
   * Get data.
   *
   * @return step data
   */
  public Optional<byte[]> getData() {
    return Optional.ofNullable(this.data);
  }

  /**
   * Create sparse Merkle tree path step from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return sparse Merkle tree path step
   */
  public static SparseMerkleTreePathStep fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SparseMerkleTreePathStep(
        BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(0))),
        CborDeserializer.readOptional(data.get(1), CborDeserializer::readByteString)
    );
  }

  /**
   * Convert sparse Merkle tree path step to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeByteString(BigIntegerConverter.encode(this.path)),
        CborSerializer.encodeOptional(this.data, CborSerializer::encodeByteString)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleTreePathStep)) {
      return false;
    }
    SparseMerkleTreePathStep that = (SparseMerkleTreePathStep) o;
    return Objects.equals(this.path, that.path) && Arrays.equals(this.data, that.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, Arrays.hashCode(this.data));
  }

  @Override
  public String toString() {
    return String.format(
        "MerkleTreePathStep{path=%s, data=%s}",
        this.path.toString(2),
        this.data == null ? "null" : HexConverter.encode(this.data)
    );
  }
}
