package com.unicity.sdk.smt;

public interface NodeBranch extends Branch {
    Branch getLeft();

    Branch getRight();
}
