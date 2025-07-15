package com.unicity.sdk.shared.smt.path;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.FinalizedBranch;
import com.unicity.sdk.shared.smt.FinalizedLeafBranch;
import com.unicity.sdk.shared.smt.FinalizedNodeBranch;

import java.math.BigInteger;
import java.util.Objects;

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
        this(
                path,
                sibling != null ? sibling.getHash() : null,
                value != null ? new MerkleTreePathStepBranch(value) : null
        );
    }

    public MerkleTreePathStep(BigInteger path, DataHash sibling, MerkleTreePathStepBranch branch) {
        if (path == null) {
            throw new IllegalArgumentException("Invalid path: null");
        }

        this.path = path;
        this.sibling = sibling;
        this.branch = branch;
    }

    public BigInteger getPath() {
        return this.path;
    }

    public DataHash getSibling() {
        return this.sibling;
    }

    public MerkleTreePathStepBranch getBranch() {
        return this.branch;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MerkleTreePathStep)) return false;
        MerkleTreePathStep that = (MerkleTreePathStep) o;
        return Objects.equals(path, that.path) && Objects.equals(sibling, that.sibling) && Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, sibling, branch);
    }
}
