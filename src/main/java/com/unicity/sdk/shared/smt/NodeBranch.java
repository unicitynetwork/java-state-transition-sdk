package com.unicity.sdk.shared.smt;

public interface NodeBranch extends Branch {
    Branch getLeft();

    Branch getRight();
}
