package com.unicity.sdk.mtree.plain;

interface NodeBranch extends Branch {
    Branch getLeft();

    Branch getRight();
}
