package com.unicity.sdk.shared.smt;

import com.unicity.sdk.shared.hash.DataHash;

public interface FinalizedBranch extends Branch {
    DataHash getHash();
}
