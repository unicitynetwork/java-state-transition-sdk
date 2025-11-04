package org.unicitylabs.sdk.mtree.sum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.MerkleTreePathVerificationResult;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.serializer.json.BigIntegerAsStringSerializer;
import org.unicitylabs.sdk.serializer.json.LongAsStringSerializer;
import org.unicitylabs.sdk.util.BigIntegerConverter;

/**
 * Path in a sparse merkle sum tree.
 */
public class SparseMerkleSumTreePath {

  private final Root root;
  private final List<SparseMerkleSumTreePathStep> steps;

  @JsonCreator
  SparseMerkleSumTreePath(
      @JsonProperty("root") Root root,
      @JsonProperty("steps") List<SparseMerkleSumTreePathStep> steps
  ) {
    Objects.requireNonNull(root, "root cannot be null");
    Objects.requireNonNull(steps, "steps cannot be null");

    this.root = root;
    this.steps = Collections.unmodifiableList(new ArrayList<>(steps));
  }

  /**
   * Get root of the path.
   *
   * @return root
   */
  public Root getRoot() {
    return this.root;
  }

  /**
   * Get steps of the path from leaf to the root.
   *
   * @return steps
   */
  public List<SparseMerkleSumTreePathStep> getSteps() {
    return this.steps;
  }

  /**
   * Verify the path against the given state ID.
   *
   * @param stateId state ID to verify against
   * @return result of the verification
   */
  public MerkleTreePathVerificationResult verify(BigInteger stateId) {
    BigInteger currentPath = BigInteger.ONE;
    DataHash currentHash = null;
    BigInteger currentCounter = this.steps.isEmpty()
        ? BigInteger.ZERO
        : this.steps.get(0)
            .getBranch()
            .map(SparseMerkleSumTreePathStep.Branch::getCounter)
            .orElse(BigInteger.ZERO);

    for (int i = 0; i < this.steps.size(); i++) {
      SparseMerkleSumTreePathStep step = this.steps.get(i);
      DataHash hash = null;

      if (step.getBranch().isPresent()) {
        byte[] bytes = i == 0
            ? step.getBranch().get().getValue()
            : (currentHash != null ? currentHash.getImprint() : null);
        hash = new DataHasher(HashAlgorithm.SHA256)
            .update(
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(
                        BigIntegerConverter.encode(step.getPath())
                    ),
                    CborSerializer.encodeByteString(bytes),
                    CborSerializer.encodeByteString(BigIntegerConverter.encode(currentCounter))
                )
            )
            .digest();

        int length = step.getPath().bitLength() - 1;
        currentPath = currentPath.shiftLeft(length)
            .or(step.getPath().and(BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE)));
      }

      boolean isRight = step.getPath().testBit(0);
      byte[] sibling = step.getSibling()
          .map(
              data -> CborSerializer.encodeArray(
                  CborSerializer.encodeByteString(data.getValue()),
                  CborSerializer.encodeByteString(BigIntegerConverter.encode(data.getCounter()))
              )
          ).orElse(CborSerializer.encodeNull());
      byte[] branch = hash == null
          ? CborSerializer.encodeNull()
          : CborSerializer.encodeArray(
              CborSerializer.encodeByteString(hash.getImprint()),
              CborSerializer.encodeByteString(BigIntegerConverter.encode(currentCounter))
          );

      currentHash = new DataHasher(HashAlgorithm.SHA256)
          .update(
              CborSerializer.encodeArray(
                  isRight ? sibling : branch,
                  isRight ? branch : sibling
              )
          )
          .digest();
      currentCounter = currentCounter.add(
          step.getSibling()
              .map(SparseMerkleSumTreePathStep.Branch::getCounter)
              .orElse(BigInteger.ZERO)
      );
    }

    return new MerkleTreePathVerificationResult(
        this.root.hash.equals(currentHash) && this.root.counter.equals(currentCounter),
        currentPath.equals(stateId));
  }

  /**
   * Create path from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return path
   */
  public static SparseMerkleSumTreePath fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SparseMerkleSumTreePath(
        SparseMerkleSumTreePath.Root.fromCbor(data.get(0)),
        CborDeserializer.readArray(data.get(1)).stream()
            .map(SparseMerkleSumTreePathStep::fromCbor)
            .collect(Collectors.toList())
    );
  }

  /**
   * Convert path to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        this.root.toCbor(),
        CborSerializer.encodeArray(
            this.steps.stream()
                .map(SparseMerkleSumTreePathStep::toCbor)
                .toArray(byte[][]::new)
        )
    );
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SparseMerkleSumTreePath)) {
      return false;
    }
    SparseMerkleSumTreePath that = (SparseMerkleSumTreePath) o;
    return Objects.equals(this.root, that.root) && Objects.equals(this.steps, that.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.root, this.steps);
  }

  @Override
  public String toString() {
    return String.format("MerkleTreePath{root=%s, steps=%s}", this.root, this.steps);
  }

  /**
   * Root of the sparse merkle sum tree path.
   */
  public static class Root {

    private final DataHash hash;
    private final BigInteger counter;

    @JsonCreator
    Root(
        @JsonProperty("hash") DataHash hash,
        @JsonProperty("counter") BigInteger counter
    ) {
      this.hash = Objects.requireNonNull(hash, "hash cannot be null");
      this.counter = Objects.requireNonNull(counter, "counter cannot be null");
    }

    /**
     * Get hash of the root.
     *
     * @return hash
     */
    public DataHash getHash() {
      return this.hash;
    }

    /**
     * Get the counter of the root.
     *
     * @return counter
     */
    @JsonSerialize(using = BigIntegerAsStringSerializer.class)
    public BigInteger getCounter() {
      return this.counter;
    }

    /**
     * Create root from CBOR bytes.
     *
     * @param bytes CBOR bytes
     * @return root
     */
    public static Root fromCbor(byte[] bytes) {
      List<byte[]> data = CborDeserializer.readArray(bytes);

      return new Root(
          DataHash.fromCbor(data.get(0)),
          BigIntegerConverter.decode(CborDeserializer.readByteString(data.get(1)))
      );
    }

    /**
     * Convert root to CBOR bytes.
     *
     * @return CBOR bytes
     */
    public byte[] toCbor() {
      return CborSerializer.encodeArray(
          this.hash.toCbor(),
          CborSerializer.encodeByteString(BigIntegerConverter.encode(this.counter))
      );
    }

    @Override
    public String toString() {
      return String.format("Root{hash=%s, counter=%s}", this.hash, this.counter);
    }
  }
}
