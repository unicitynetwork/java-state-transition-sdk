package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
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
        DataHash hash = new DataHasher(hashAlgorithm).update(this.path.toByteArray()).update(this.value).digest();
        return new FinalizedLeafBranch(this.path, this.value, hash);
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
