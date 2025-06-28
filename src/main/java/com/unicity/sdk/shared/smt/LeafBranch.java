
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;

public class LeafBranch implements Branch {
    private final DataHash hash;

    public LeafBranch(DataHash hash) {
        this.hash = hash;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }
}
