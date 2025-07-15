package com.unicity.sdk.shared.smt.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.FinalizedBranch;
import com.unicity.sdk.shared.smt.FinalizedLeafBranch;
import com.unicity.sdk.shared.smt.FinalizedNodeBranch;

import java.math.BigInteger;

public class MerkleTreePathStep {
    private final BigInteger path;
    private final DataHash sibling;
    private final MerkleTreePathStepBranch branch;

    public MerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedLeafBranch branch) {
        this(path, sibling, branch == null ? null : branch.getValue());
    }

    public MerkleTreePathStep(BigInteger path, FinalizedBranch sibling, FinalizedNodeBranch branch) {
        this(path, sibling, branch == null ? null : branch.getChildrenHash().getData());
    }

    public MerkleTreePathStep(BigInteger path, FinalizedBranch sibling, byte[] value) {
        this.path = path;
        this.sibling = sibling != null ? sibling.getHash() : null;
        this.branch = value != null ? new MerkleTreePathStepBranch(value) : null;
    }

    @JsonProperty("path")
    @JsonSerialize(using = ToStringSerializer.class)
    public BigInteger getPath() {
        return this.path;
    }

    @JsonProperty("sibling")
    public DataHash getSibling() {
        return this.sibling;
    }

    @JsonProperty("branch")
    public MerkleTreePathStepBranch getBranch() {
        return this.branch;
    }
}
