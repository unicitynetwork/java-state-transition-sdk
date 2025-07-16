package com.unicity.sdk.smt;

public class BranchExistsException extends Exception {
    public BranchExistsException() {
        super("Branch already exists at this path.");
    }
}
