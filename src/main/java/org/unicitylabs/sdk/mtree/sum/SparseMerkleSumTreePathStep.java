package org.unicitylabs.sdk.mtree.sum;

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
import org.unicitylabs.sdk.util.BigIntegerConverter;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Step in a sparse merkle sum tree path.
 */
public class SparseMerkleSumTreePathStep {

  private final BigInteger path;
  private final Branch sibling;
  private final Branch branch;

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling,
      FinalizedLeafBranch branch) {
    this(
        path,
        sibling,
        branch == null
            ? null
            : new Branch(branch.getValue().getValue(), branch.getValue().getCounter())
    );
  }

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling,
      FinalizedNodeBranch branch) {
    this(
        path,
        sibling,
        branch == null ? null
            : new Branch(branch.getChildrenHash().getImprint(), branch.getCounter())
    );
  }

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling) {
    this(
        path,
        sibling,
        (Branch) null
    );
  }

  SparseMerkleSumTreePathStep(BigInteger path, FinalizedBranch sibling, Branch branch) {
    this(
        path,
        sibling == null ? null : new Branch(sibling.getHash().getImprint(), sibling.getCounter()),
        branch
    );
  }

  @JsonCreator
  SparseMerkleSumTreePathStep(
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
   * Get path of the step.
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
   * Get branch at this step (can be null for non-leaf steps).
   *
   * @return branch
   */
  public Optional<Branch> getBranch() {
    return Optional.ofNullable(this.branch);
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
        Branch.fromCbor(data.get(1)),
        Branch.fromCbor(data.get(2))
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
        CborSerializer.encodeOptional(this.sibling, Branch::toCbor),
        CborSerializer.encodeOptional(this.branch, Branch::toCbor)
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleSumTreePathStep)) {
      return false;
    }
    SparseMerkleSumTreePathStep that = (SparseMerkleSumTreePathStep) o;
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
   * A branch in the sparse merkle sum tree.
   */
  @JsonSerialize(using = SparseMerkleSumTreePathStepBranchJson.Serializer.class)
  @JsonDeserialize(using = SparseMerkleSumTreePathStepBranchJson.Deserializer.class)
  public static class Branch {

    private final byte[] value;
    private final BigInteger counter;

    Branch(byte[] value, BigInteger counter) {
      Objects.requireNonNull(counter, "counter cannot be null");

      this.value = value == null ? null : Arrays.copyOf(value, value.length);
      this.counter = counter;
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
     * Get the counter.
     *
     * @return counter
     */
    public BigInteger getCounter() {
      return this.counter;
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
          CborDeserializer.readByteString(data.get(0)),
          BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(1)))
      );
    }

    /**
     * Convert branch to CBOR bytes.
     *
     * @return CBOR bytes
     */
    public byte[] toCbor() {
      return CborSerializer.encodeArray(
          CborSerializer.encodeOptional(this.value, CborSerializer::encodeByteString),
          CborSerializer.encodeByteString(BigIntegerConverter.encode(this.counter))
      );
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Branch)) {
        return false;
      }
      Branch branch = (Branch) o;
      return Objects.deepEquals(this.value, branch.value) && Objects.equals(this.counter,
          branch.counter);
    }

    @Override
    public int hashCode() {
      return Objects.hash(Arrays.hashCode(this.value), this.counter);
    }

    @Override
    public String toString() {
      return String.format(
          "Branch{value=%s, counter=%s}",
          this.value == null ? null : HexConverter.encode(this.value),
          this.counter
      );
    }
  }
}
