package org.unicitylabs.sdk.mtree.plain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.BigIntegerAsStringSerializer;
import org.unicitylabs.sdk.serializer.json.LongAsStringSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Sparse Merkle tree path step.
 */
public class SparseMerkleTreePathStep {

  private final BigInteger path;
  private final Branch sibling;
  private final Branch branch;

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedLeafBranch branch) {
    this(
        path,
        sibling,
        branch == null
            ? null
            : new Branch(branch.getValue())
    );
  }

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling,
      FinalizedNodeBranch branch) {
    this(
        path,
        sibling,
        branch == null ? null
            : new Branch(branch.getChildrenHash().getData())
    );
  }

  SparseMerkleTreePathStep(BigInteger path, FinalizedBranch sibling, Branch branch) {
    this(
        path,
        sibling == null ? null : new Branch(sibling.getHash().getData()),
        branch
    );
  }

  @JsonCreator
  SparseMerkleTreePathStep(
      @JsonProperty("path") BigInteger path,
      @JsonProperty("sibling") Branch sibling,
      @JsonProperty("branch") Branch branch
  ) {
    Objects.requireNonNull(path, "path cannot be null");

    this.path = path;
    this.sibling = sibling;
    this.branch = branch;
  }

  /**
   * Get path.
   *
   * @return path
   */
  @JsonSerialize(using = BigIntegerAsStringSerializer.class)
  public BigInteger getPath() {
    return this.path;
  }

  /**
   * Get sibling branch.
   *
   * @return sibling branch
   */
  public Optional<Branch> getSibling() {
    return Optional.ofNullable(this.sibling);
  }

  /**
   * Get branch.
   *
   * @return branch
   */
  public Optional<Branch> getBranch() {
    return Optional.ofNullable(this.branch);
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
        Branch.fromCbor(data.get(1)),
        Branch.fromCbor(data.get(2))
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
        CborSerializer.encodeOptional(this.sibling, Branch::toCbor),
        CborSerializer.encodeOptional(this.branch, Branch::toCbor)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleTreePathStep)) {
      return false;
    }
    SparseMerkleTreePathStep that = (SparseMerkleTreePathStep) o;
    return Objects.equals(this.path, that.path) && Objects.equals(this.sibling, that.sibling)
        && Objects.equals(this.branch, that.branch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.path, this.sibling, this.branch);
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePathStep{path=%s, sibling=%s, branch=%s}",
        this.path.toString(2), this.sibling, this.branch);
  }

  /**
   * Sparse Merkle tree branch.
   */
  @JsonSerialize(using = SparseMerkleTreePathStepBranchJson.Serializer.class)
  @JsonDeserialize(using = SparseMerkleTreePathStepBranchJson.Deserializer.class)
  public static class Branch {

    private final byte[] value;

    Branch(byte[] value) {
      this.value = value == null ? null : Arrays.copyOf(value, value.length);
    }

    /**
     * Get branch value.
     *
     * @return value
     */
    public byte[] getValue() {
      return this.value == null ? null : Arrays.copyOf(this.value, this.value.length);
    }

    /**
     * Create branch from CBOR bytes.
     *
     * @param bytes CBOR bytes
     * @return branch
     */
    public static Branch fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);

      return new Branch(
          CborDeserializer.readByteString(data.get(0))
      );
    }

    /**
     * Convert branch to CBOR bytes.
     *
     * @return CBOR bytes
     */
    public byte[] toCbor() {
      return CborSerializer.encodeArray(
          CborSerializer.encodeOptional(this.value, CborSerializer::encodeByteString)
      );
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Branch)) {
        return false;
      }
      Branch branch = (Branch) o;
      return Objects.deepEquals(this.value, branch.value);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(this.value);
    }

    @Override
    public String toString() {
      return String.format(
          "Branch{value=%s}",
          this.value == null ? null : HexConverter.encode(this.value)
      );
    }
  }
}
