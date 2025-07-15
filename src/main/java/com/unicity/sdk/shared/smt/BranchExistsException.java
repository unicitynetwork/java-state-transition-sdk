package com.unicity.sdk.shared.smt;

public class BranchExistsException extends Exception {
    public BranchExistsException() {
        super("Branch already exists at this path.");
    }
}
