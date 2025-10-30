package org.unicitylabs.sdk.mtree.plain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.mtree.MerkleTreePathVerificationResult;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;
import org.unicitylabs.sdk.util.BigIntegerConverter;

/**
 * Sparse merkle tree path for selected path.
 */
public class SparseMerkleTreePath {

  private final DataHash rootHash;
  private final List<SparseMerkleTreePathStep> steps;

  @JsonCreator
  SparseMerkleTreePath(
      @JsonProperty("root")
      DataHash rootHash,
      @JsonProperty("steps")
      List<SparseMerkleTreePathStep> steps
  ) {
    Objects.requireNonNull(rootHash, "rootHash cannot be null");
    Objects.requireNonNull(steps, "steps cannot be null");

    this.rootHash = rootHash;
    this.steps = List.copyOf(steps);
  }

  /**
   * Get root hash.
   *
   * @return root hash
   */
  @JsonGetter("root")
  public DataHash getRootHash() {
    return this.rootHash;
  }

  /**
   * Get steps to root.
   *
   * @return steps
   */
  public List<SparseMerkleTreePathStep> getSteps() {
    return this.steps;
  }

  /**
   * Verify merkle tree path against given path.
   *
   * @param requestId path
   * @return true if successful
   */
  public MerkleTreePathVerificationResult verify(BigInteger requestId) {
    if (this.steps.size() == 0) {
      return new MerkleTreePathVerificationResult(false, false);
    }

    SparseMerkleTreePathStep step = this.steps.get(0);
    byte[] currentData;
    BigInteger currentPath = BigInteger.ONE;
    if (step.getPath().compareTo(BigInteger.ZERO) > 0) {
      DataHash hash = new DataHasher(this.rootHash.getAlgorithm())
          .update(
              CborSerializer.encodeArray(
                  CborSerializer.encodeByteString(BigIntegerConverter.encode(step.getPath())),
                  CborSerializer.encodeOptional(
                      step.getData().orElse(null),
                      CborSerializer::encodeByteString
                  )
              )
          )
          .digest();

      currentData = hash.getData();

      int length = step.getPath().bitLength() - 1;
      currentPath = currentPath.shiftLeft(length)
          .or(step.getPath().and(BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE)));
    } else {
      currentData = step.getData().orElse(null);
    }

    for (int i = 1; i < this.steps.size(); i++) {
      step = this.steps.get(i);
      boolean isRight = currentPath.testBit(0);

      byte[] left = isRight ? step.getData().orElse(null) : currentData;
      byte[] right = isRight ? currentData : step.getData().orElse(null);

      DataHash hash = new DataHasher(this.rootHash.getAlgorithm())
          .update(
              CborSerializer.encodeArray(
                  CborSerializer.encodeByteString(BigIntegerConverter.encode(step.getPath())),
                  CborSerializer.encodeOptional(left, CborSerializer::encodeByteString),
                  CborSerializer.encodeOptional(right, CborSerializer::encodeByteString)
                  )
              )
          .digest();

      currentData = hash.getData();

      int length = step.getPath().bitLength() - 1;
      currentPath = currentPath.shiftLeft(length)
          .or(step.getPath().and(BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE)));
    }

    boolean pathValid = currentData != null
        && this.rootHash.equals(new DataHash(this.rootHash.getAlgorithm(), currentData));
    boolean pathIncluded = currentPath.compareTo(requestId) == 0;

    return new MerkleTreePathVerificationResult(pathValid, pathIncluded);
  }

  /**
   * Create sparse merkle tree path from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return path
   */
  public static SparseMerkleTreePath fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SparseMerkleTreePath(
        DataHash.fromCbor(data.get(0)),
        CborDeserializer.readArray(data.get(1)).stream()
            .map(SparseMerkleTreePathStep::fromCbor)
            .collect(Collectors.toList())
    );
  }

  /**
   * Convert sparse merkle tree path to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        this.rootHash.toCbor(),
        CborSerializer.encodeArray(
            this.steps.stream()
                .map(SparseMerkleTreePathStep::toCbor)
                .toArray(byte[][]::new)
        )
    );
  }

  /**
   * Create sparse merkle tree path from JSON string.
   *
   * @param input JSON string
   * @return path
   */
  public static SparseMerkleTreePath fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, SparseMerkleTreePath.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SparseMerkleTreePath.class, e);
    }
  }

  /**
   * Convert sparse merkle tree path to JSON string.
   *
   * @return JSON string
   */
  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(SparseMerkleTreePath.class, e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleTreePath)) {
      return false;
    }
    SparseMerkleTreePath that = (SparseMerkleTreePath) o;
    return Objects.equals(this.rootHash, that.rootHash) && Objects.equals(this.steps, that.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.rootHash, this.steps);
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePath{rootHash=%s, steps=%s}", this.rootHash, this.steps);
  }
}
