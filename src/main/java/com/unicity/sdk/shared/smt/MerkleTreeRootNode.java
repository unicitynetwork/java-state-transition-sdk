
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;

public class MerkleTreeRootNode {
    private final DataHash hash;

    public MerkleTreeRootNode(DataHash hash) {
        this.hash = hash;
    }

    public DataHash getHash() {
        return hash;
    }
}
