package org.unicitylabs.sdk.mtree.plain;

interface NodeBranch extends Branch {
    Branch getLeft();

    Branch getRight();
}
