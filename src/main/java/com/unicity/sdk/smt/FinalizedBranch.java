package com.unicity.sdk.smt;

import com.unicity.sdk.hash.DataHash;

public interface FinalizedBranch extends Branch {
    DataHash getHash();
}
