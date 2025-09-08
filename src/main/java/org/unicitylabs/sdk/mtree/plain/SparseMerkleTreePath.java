package org.unicitylabs.sdk.mtree.plain;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.mtree.MerkleTreePathVerificationResult;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

public class SparseMerkleTreePath {

  private final DataHash rootHash;
  private final List<SparseMerkleTreePathStep> steps;

  public SparseMerkleTreePath(DataHash rootHash, List<SparseMerkleTreePathStep> steps) {
    Objects.requireNonNull(rootHash, "rootHash cannot be null");
    Objects.requireNonNull(steps, "steps cannot be null");

    this.rootHash = rootHash;
    this.steps = List.copyOf(steps);
  }

  public DataHash getRootHash() {
    return this.rootHash;
  }

  public List<SparseMerkleTreePathStep> getSteps() {
    return this.steps;
  }

  public MerkleTreePathVerificationResult verify(BigInteger requestId) {
    BigInteger currentPath = BigInteger.ONE; // Root path is always 1
    DataHash currentHash = null;

    for (int i = 0; i < this.steps.size(); i++) {
      SparseMerkleTreePathStep step = this.steps.get(i);
      byte[] hash;
      if (step.getBranch().isEmpty()) {
        hash = new byte[]{0};
      } else {
        byte[] bytes = i == 0
            ? step.getBranch().map(SparseMerkleTreePathStep.Branch::getValue).orElse(null)
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

      byte[] siblingHash = step.getSibling().map(SparseMerkleTreePathStep.Branch::getValue).orElse(new byte[]{0});
      boolean isRight = step.getPath().testBit(0);
      currentHash = new DataHasher(HashAlgorithm.SHA256).update(isRight ? siblingHash : hash)
          .update(isRight ? hash : siblingHash).digest();
    }

    return new MerkleTreePathVerificationResult(this.rootHash.equals(currentHash),
        currentPath.equals(requestId));
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
