package com.unicity.sdk.mtree.sum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.MerkleTreePathVerificationResult;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.serializer.cbor.CborSerializationException;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class SparseMerkleSumTreePath {

  private final Root root;
  private final List<SparseMerkleSumTreePathStep> steps;

  public SparseMerkleSumTreePath(Root root, List<SparseMerkleSumTreePathStep> steps) {
    Objects.requireNonNull(root, "root cannot be null");
    Objects.requireNonNull(steps, "steps cannot be null");

    this.root = root;
    this.steps = List.copyOf(steps);
  }

  public Root getRoot() {
    return this.root;
  }

  public List<SparseMerkleSumTreePathStep> getSteps() {
    return this.steps;
  }

  // TODO: Make it possible to use other hash algorithms
  public MerkleTreePathVerificationResult verify(BigInteger requestId) {
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
        try {
          hash = new DataHasher(HashAlgorithm.SHA256)
              .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
                  UnicityObjectMapper.CBOR.createArrayNode()
                      .add(BigIntegerConverter.encode(step.getPath()))
                      .add(bytes)
                      .add(BigIntegerConverter.encode(currentCounter))
              ))
              .digest();
        } catch (JsonProcessingException e) {
          throw new CborSerializationException(e);
        }

        int length = step.getPath().bitLength() - 1;
        currentPath = currentPath.shiftLeft(length)
            .or(step.getPath().and(BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE)));

      }

      boolean isRight = step.getPath().testBit(0);
      ArrayNode sibling = step.getSibling()
          .map(data -> UnicityObjectMapper.CBOR.createArrayNode()
              .add(data.getValue())
              .add(BigIntegerConverter.encode(data.getCounter())))
          .orElse(null);
      ArrayNode branch = hash == null
          ? null
          : UnicityObjectMapper.CBOR.createArrayNode()
              .add(hash.getImprint())
              .add(BigIntegerConverter.encode(currentCounter));

      try {
        currentHash = new DataHasher(HashAlgorithm.SHA256)
            .update(
                UnicityObjectMapper.CBOR.writeValueAsBytes(
                    UnicityObjectMapper.CBOR.createArrayNode()
                        .add(isRight ? sibling : branch)
                        .add(isRight ? branch : sibling)
                )
            )
            .digest();
      } catch (JsonProcessingException e) {
        throw new CborSerializationException(e);
      }
      currentCounter = currentCounter.add(
          step.getSibling()
              .map(SparseMerkleSumTreePathStep.Branch::getCounter)
              .orElse(BigInteger.ZERO)
      );
    }

    return new MerkleTreePathVerificationResult(
        this.root.hash.equals(currentHash) && this.root.counter.equals(currentCounter),
        currentPath.equals(requestId));
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

  public static class Root {

    private final DataHash hash;
    private final BigInteger counter;

    public Root(DataHash hash, BigInteger counter) {
      this.hash = Objects.requireNonNull(hash, "hash cannot be null");
      this.counter = Objects.requireNonNull(counter, "counter cannot be null");
    }

    public DataHash getHash() {
      return this.hash;
    }

    public BigInteger getCounter() {
      return this.counter;
    }
  }
}
