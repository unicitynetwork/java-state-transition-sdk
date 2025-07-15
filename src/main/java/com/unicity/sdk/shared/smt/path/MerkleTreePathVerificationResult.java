package com.unicity.sdk.shared.smt.path;

public class MerkleTreePathVerificationResult {
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

    public boolean isValid() {
        return pathValid && pathIncluded;
    }
}
