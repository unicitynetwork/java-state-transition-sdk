
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;

public class MerkleTreePathStep {
    private final DataHash hash;
    private final boolean isRight;

    public MerkleTreePathStep(DataHash hash, boolean isRight) {
        this.hash = hash;
        this.isRight = isRight;
    }

    public DataHash getHash() {
        return hash;
    }

    public boolean isRight() {
        return isRight;
    }
}
