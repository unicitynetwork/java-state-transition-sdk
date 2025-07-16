package com.unicity.sdk.smt;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.Objects;

public class FinalizedNodeBranch implements NodeBranch, FinalizedBranch {
    private final BigInteger path;
    private final FinalizedBranch left;
    private final FinalizedBranch right;
    private final DataHash hash;
    private final DataHash childrenHash;

    public FinalizedNodeBranch(BigInteger path, FinalizedBranch left, FinalizedBranch right, DataHash childrenHash, DataHash hash) {
        this.path = path;
        this.left = left;
        this.right = right;
        this.childrenHash = childrenHash;
        this.hash = hash;
    }

    public BigInteger getPath() {
        return this.path;
    }

    public FinalizedBranch getLeft() {
        return this.left;
    }

    public FinalizedBranch getRight() {
        return this.right;
    }

    public DataHash getChildrenHash() {
        return this.childrenHash;
    }

    public DataHash getHash() {
        return this.hash;
    }

    public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) {
        return this; // Already finalized
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FinalizedNodeBranch)) return false;
        FinalizedNodeBranch that = (FinalizedNodeBranch) o;
        return Objects.equals(path, that.path) && Objects.equals(left, that.left) && Objects.equals(right, that.right) && Objects.equals(hash, that.hash) && Objects.equals(childrenHash, that.childrenHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, left, right, hash, childrenHash);
    }
}
