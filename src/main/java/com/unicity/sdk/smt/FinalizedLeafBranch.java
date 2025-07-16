package com.unicity.sdk.smt;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;

public class FinalizedLeafBranch implements LeafBranch, FinalizedBranch {
    private final BigInteger path;
    private final byte[] value;
    private final DataHash hash;

    public FinalizedLeafBranch(BigInteger path, byte[] value, DataHash hash) {
        this.path = path;
        this.value = value;
        this.hash = hash;
    }

    public BigInteger getPath() {
        return this.path;
    }

    public byte[] getValue() {
        return this.value;
    }

    public DataHash getHash() {
        return this.hash;
    }

    public FinalizedLeafBranch finalize(HashAlgorithm hashAlgorithm) {
        return this; // Already finalized
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FinalizedLeafBranch)) return false;
        FinalizedLeafBranch that = (FinalizedLeafBranch) o;
        return Objects.equals(path, that.path) && Objects.deepEquals(value, that.value) && Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, Arrays.hashCode(value), hash);
    }
}
