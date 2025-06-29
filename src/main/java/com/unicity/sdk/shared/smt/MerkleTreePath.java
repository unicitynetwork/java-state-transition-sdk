
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MerkleTreePath implements ISerializable {
    private final List<MerkleTreePathStep> steps;

    public MerkleTreePath(List<MerkleTreePathStep> steps) {
        this.steps = steps;
    }

    public List<MerkleTreePathStep> getSteps() {
        return steps;
    }

    public CompletableFuture<MerkleTreePathVerificationResult> verify(BigInteger requestId) {
        // TODO: Implement merkle tree path verification
        return CompletableFuture.completedFuture(new MerkleTreePathVerificationResult(true, true));
    }

    @Override
    public Object toJSON() {
        // TODO: Implement JSON serialization
        return null;
    }

    @Override
    public byte[] toCBOR() {
        // TODO: Implement CBOR serialization
        return CborEncoder.encodeNull();
    }

    public static class MerkleTreePathVerificationResult {
        private final boolean pathValid;
        private final boolean pathIncluded;

        public MerkleTreePathVerificationResult(boolean pathValid, boolean pathIncluded) {
            this.pathValid = pathValid;
            this.pathIncluded = pathIncluded;
        }

        public boolean isPathValid() {
            return pathValid;
        }

        public boolean isPathIncluded() {
            return pathIncluded;
        }
    }
}
