package com.unicity.sdk.mtree.sum;

import com.unicity.sdk.mtree.sum.SparseMerkleSumTree.LeafValue;

interface LeafBranch extends Branch {
    LeafValue getValue();
}
