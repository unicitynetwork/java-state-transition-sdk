package com.unicity.sdk.mtree.plain;

import com.unicity.sdk.hash.DataHash;

interface FinalizedBranch extends Branch {
    DataHash getHash();
}
