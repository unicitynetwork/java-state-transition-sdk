package com.unicity.sdk.shared.smt.path;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;

@JsonSerialize(using = MerkleTreePathStepBranchSerializer.class)
public class MerkleTreePathStepBranch {
    private final byte[] value;

    public MerkleTreePathStepBranch(byte[] value) {
        this.value = value;
    }

    public byte[] getValue() {
        return Arrays.copyOf(this.value, this.value.length);
    }
}
