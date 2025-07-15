package com.unicity.sdk.shared.smt.path;

import java.util.Arrays;
import java.util.Objects;

public class MerkleTreePathStepBranch {
    private final byte[] value;

    public MerkleTreePathStepBranch(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return this.value != null ? Arrays.copyOf(this.value, this.value.length) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MerkleTreePathStepBranch)) return false;
        MerkleTreePathStepBranch that = (MerkleTreePathStepBranch) o;
        return Arrays.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }
}
