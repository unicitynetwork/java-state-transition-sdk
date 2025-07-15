package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.Objects;

public class PendingNodeBranch implements NodeBranch {
    private final BigInteger path;
    private final Branch left;
    private final Branch right;

    public PendingNodeBranch(BigInteger path, Branch left, Branch right) {
        this.path = path;
        this.left = left;
        this.right = right;
    }

    public BigInteger getPath() {
        return this.path;
    }

    public Branch getLeft() {
        return this.left;
    }

    public Branch getRight() {
        return this.right;
    }

    public FinalizedNodeBranch finalize(HashAlgorithm hashAlgorithm) throws Exception {
        FinalizedBranch left = this.left.finalize(hashAlgorithm);
        FinalizedBranch right = this.right.finalize(hashAlgorithm);
        DataHash childrenHash = new DataHasher(hashAlgorithm).update(left.getHash().getData()).update(right.getHash().getData()).digest();
        DataHash hash = new DataHasher(hashAlgorithm).update(this.path.toByteArray()).update(childrenHash.getData()).digest();

        return new FinalizedNodeBranch(this.path, left, right, childrenHash, hash);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PendingNodeBranch)) return false;
        PendingNodeBranch that = (PendingNodeBranch) o;
        return Objects.equals(path, that.path) && Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, left, right);
    }
}
