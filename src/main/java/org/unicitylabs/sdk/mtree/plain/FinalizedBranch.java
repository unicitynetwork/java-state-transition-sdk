package org.unicitylabs.sdk.mtree.plain;

import org.unicitylabs.sdk.hash.DataHash;

interface FinalizedBranch extends Branch {
    DataHash getHash();
}
