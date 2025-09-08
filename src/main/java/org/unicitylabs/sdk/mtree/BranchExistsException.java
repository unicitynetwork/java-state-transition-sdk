package org.unicitylabs.sdk.mtree;

public class BranchExistsException extends Exception {
    public BranchExistsException() {
        super("Branch already exists at this path.");
    }
}
