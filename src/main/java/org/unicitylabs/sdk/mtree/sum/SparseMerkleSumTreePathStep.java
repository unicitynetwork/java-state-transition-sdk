package org.unicitylabs.sdk.mtree.sum;

import java.util.List;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePathStep;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import org.unicitylabs.sdk.util.HexConverter;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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

  public SparseMerkleSumTreePathStep(BigInteger path, Branch sibling, Branch branch) {
    Objects.requireNonNull(path, "path cannot be null");

    this.path = path;
    this.sibling = sibling;
    this.branch = branch;
  }

  public BigInteger getPath() {
    return this.path;
  }

  public Optional<Branch> getSibling() {
    return Optional.ofNullable(this.sibling);
  }

  public Optional<Branch> getBranch() {
    return Optional.ofNullable(this.branch);
  }

  public static SparseMerkleSumTreePathStep fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SparseMerkleSumTreePathStep(
        BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(0))),
        Branch.fromCbor(data.get(1)),
        Branch.fromCbor(data.get(2))
    );
  }

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

  public static class Branch {

    private final byte[] value;
    private final BigInteger counter;

    public Branch(byte[] value, BigInteger counter) {
      Objects.requireNonNull(counter, "counter cannot be null");

      this.value = value == null ? null : Arrays.copyOf(value, value.length);
      this.counter = counter;
    }

    public byte[] getValue() {
      return this.value == null ? null : Arrays.copyOf(this.value, this.value.length);
    }

    public BigInteger getCounter() {
      return this.counter;
    }

    public static Branch fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);

      return new Branch(
          CborDeserializer.readByteString(data.get(0)),
          BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(1)))
      );
    }

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
