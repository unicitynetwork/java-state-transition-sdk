package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

public class PendingLeafBranch implements LeafBranch {
    private final BigInteger path;
    private final byte[] value;

    public PendingLeafBranch(BigInteger path, byte[] value) {
        this.path = path;
        this.value = value;
    }

    public BigInteger getPath() {
        return this.path;
    }

    public byte[] getValue() {
        return this.value;
    }

    public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) throws Exception {
        MessageDigest hashDigest = MessageDigest.getInstance(hashAlgorithm.getAlgorithm());
        hashDigest.update(this.path.toByteArray());
        hashDigest.update(this.value);
        byte[] hash = hashDigest.digest();

        return new FinalizedLeafBranch(this.path, this.value, new DataHash(hashAlgorithm, hash));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PendingLeafBranch)) return false;
        PendingLeafBranch that = (PendingLeafBranch) o;
        return Objects.equals(path, that.path) && Objects.deepEquals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, Arrays.hashCode(value));
    }
}
