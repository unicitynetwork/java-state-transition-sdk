package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.mtree.MerkleTreePathVerificationResult;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class MerkleTreePath {

  private final Root root;
  private final List<MerkleTreePathStep> steps;

  public MerkleTreePath(Root root, List<MerkleTreePathStep> steps) {
    Objects.requireNonNull(root, "rootHash cannot be null");
    Objects.requireNonNull(steps, "steps cannot be null");

    this.root = root;
    this.steps = List.copyOf(steps);
  }

  public Root getRoot() {
    return this.root;
  }

  public List<MerkleTreePathStep> getSteps() {
    return this.steps;
  }

  public MerkleTreePathVerificationResult verify(BigInteger requestId) {
    BigInteger currentPath = BigInteger.ONE; // Root path is always 1
    DataHash currentHash = null;

    for (int i = 0; i < this.steps.size(); i++) {
      MerkleTreePathStep step = this.steps.get(i);
      byte[] hash;
      if (step.getBranch() == null) {
        hash = new byte[]{0};
      } else {
        byte[] bytes = i == 0 ? step.getBranch().getValue()
            : (currentHash != null ? currentHash.getData() : null);
        hash = new DataHasher(HashAlgorithm.SHA256)
            .update(BigIntegerConverter.encode(step.getPath()))
            .update(bytes == null ? new byte[]{0} : bytes)
            .digest()
            .getData();

        int length = step.getPath().bitLength() - 1;
        currentPath = currentPath.shiftLeft(length)
            .or(step.getPath().and(BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE)));
      }

      byte[] siblingHash = step.getSibling() != null ? step.getSibling().getValue() : new byte[]{0};
      boolean isRight = step.getPath().testBit(0);
      currentHash = new DataHasher(HashAlgorithm.SHA256).update(isRight ? siblingHash : hash)
          .update(isRight ? hash : siblingHash).digest();
    }

    return new MerkleTreePathVerificationResult(
        this.root.hash.equals(currentHash) && this.root.counter.equals(0),
        currentPath.equals(requestId));
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MerkleTreePath)) {
      return false;
    }
    MerkleTreePath that = (MerkleTreePath) o;
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
