package org.unicitylabs.sdk.mtree.sum;

import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;

interface LeafBranch extends Branch {
    LeafValue getValue();
}
