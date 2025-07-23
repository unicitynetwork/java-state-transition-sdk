package com.unicity.sdk.smt.path;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.util.BigIntegerConverter;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MerkleTreePath {
    private final DataHash rootHash;
    private final List<MerkleTreePathStep> steps;

    public MerkleTreePath(DataHash rootHash, List<MerkleTreePathStep> steps) {
        if (rootHash == null) {
            throw new IllegalArgumentException("Invalid root hash: null");
        }

        if (steps == null) {
            throw new IllegalArgumentException("Invalid steps: null");
        }

        this.rootHash = rootHash;
        this.steps = Collections.unmodifiableList(steps);
    }

    public DataHash getRootHash() {
        return this.rootHash;
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
                byte[] bytes = i == 0 ? step.getBranch().getValue() : (currentHash != null ? currentHash.getData() : null);
                hash = new DataHasher(HashAlgorithm.SHA256)
                        .update(BigIntegerConverter.encode(step.getPath()))
                        .update(bytes == null ? new byte[]{0} : bytes)
                        .digest()
                        .getData();

                int length = step.getPath().bitLength() - 1;
                currentPath = currentPath.shiftLeft(length).or(step.getPath().and(BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE)));
            }

            byte[] siblingHash = step.getSibling() != null ? step.getSibling().getData() : new byte[]{0};
            boolean isRight = step.getPath().testBit(0);
            currentHash = new DataHasher(HashAlgorithm.SHA256).update(isRight ? siblingHash : hash).update(isRight ? hash : siblingHash).digest();
        }

        return new MerkleTreePathVerificationResult(this.rootHash.equals(currentHash), currentPath.equals(requestId));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MerkleTreePath)) return false;
        MerkleTreePath that = (MerkleTreePath) o;
        return Objects.equals(rootHash, that.rootHash) && Objects.equals(steps, that.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootHash, steps);
    }

    @Override
    public String toString() {
        return String.format("MerkleTreePath{rootHash=%s, steps=%s}", rootHash, steps);
    }
}
