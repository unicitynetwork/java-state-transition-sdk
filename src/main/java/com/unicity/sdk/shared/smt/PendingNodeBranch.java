package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
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
        /*
        const hash = await factory.create().update(BigintConverter.encode(this.path)).update(this.value).digest();
    return new LeafBranch(this.path, this.value, hash);
         */
        FinalizedBranch left = this.left.finalize(hashAlgorithm);
        FinalizedBranch right = this.right.finalize(hashAlgorithm);
        MessageDigest childHashDigest = MessageDigest.getInstance(hashAlgorithm.getAlgorithm());
        childHashDigest.update(left.getHash().getData());
        childHashDigest.update(right.getHash().getData());
        byte[] childrenHash = childHashDigest.digest();

        MessageDigest hashDigest = MessageDigest.getInstance(hashAlgorithm.getAlgorithm());
        hashDigest.update(this.path.toByteArray());
        hashDigest.update(childrenHash);
        byte[] hash = hashDigest.digest();

        return new FinalizedNodeBranch(this.path, left, right, new DataHash(hashAlgorithm, childrenHash), new DataHash(hashAlgorithm, hash));
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
