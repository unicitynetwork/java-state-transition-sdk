
package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;

public class PendingLeafBranch implements PendingBranch {
    private final DataHash hash;

    public PendingLeafBranch(DataHash hash) {
        this.hash = hash;
    }

    @Override
    public DataHash getHash() {
        return hash;
    }
}
