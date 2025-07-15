package com.unicity.sdk.shared.smt.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class MerkleTreePath {
    private final DataHash rootHash;
    private final List<MerkleTreePathStep> steps;

    public MerkleTreePath(DataHash rootHash, List<MerkleTreePathStep> steps) {
        this.rootHash = rootHash;
        this.steps = steps;
    }

    @JsonProperty("root")
    public DataHash getRootHash() {
        return this.rootHash;
    }

    @JsonProperty("steps")
    public List<MerkleTreePathStep> getSteps() {
        return this.steps;
    }

    public MerkleTreePathVerificationResult verify(BigInteger requestId) throws MerkleTreePathVerificationException {
        BigInteger currentPath = BigInteger.ONE; // Root path is always 1
        DataHash currentHash = null;

        for (int i = 0; i < this.steps.size(); i++) {
            MerkleTreePathStep step = this.steps.get(i);
            byte[] hash;
            if (step.getBranch() == null) {
                hash = new byte[] { 0 };
            } else {
                byte[] bytes = i == 0 ? step.getBranch().getValue() : (currentHash != null ? currentHash.getData() : null);
                try {
                    MessageDigest digest = MessageDigest.getInstance(HashAlgorithm.SHA256.getAlgorithm());
                    digest.update(step.getPath().toByteArray());
                    digest.update(bytes == null ? new byte[] { 0 } : bytes);
                    hash = digest.digest();
                } catch (Exception e) {
                    throw new MerkleTreePathVerificationException("Error calculating step input hash", e);
                }

                int length = step.getPath().bitLength() - 1;
                currentPath = currentPath.shiftLeft(length).or(step.getPath().and(BigInteger.valueOf((1L << length) - 1)));
            }

            byte[] siblingHash = step.getSibling() != null ? step.getSibling().getData() : new byte[] { 0 };
            boolean isRight = step.getPath().testBit(0);
            try {
                MessageDigest hashDigest = MessageDigest.getInstance(HashAlgorithm.SHA256.getAlgorithm());
                if (isRight) {
                    hashDigest.update(siblingHash);
                    hashDigest.update(hash);
                } else {
                    hashDigest.update(hash);
                    hashDigest.update(siblingHash);
                }
                currentHash = new DataHash(HashAlgorithm.SHA256, hashDigest.digest());
            } catch (Exception e) {
                throw new MerkleTreePathVerificationException("Error calculating step hash", e);
            }
        }

        return new MerkleTreePathVerificationResult(this.rootHash.equals(currentHash), currentPath.equals(requestId));
    }
}
