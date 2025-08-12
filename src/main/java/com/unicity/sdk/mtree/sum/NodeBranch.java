package com.unicity.sdk.mtree.sum;

interface NodeBranch extends Branch {
    Branch getLeft();

    Branch getRight();
}
