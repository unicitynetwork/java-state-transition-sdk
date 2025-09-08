package org.unicitylabs.sdk.mtree;

public class LeafOutOfBoundsException extends Exception {
    public LeafOutOfBoundsException() {
        super("Cannot extend tree through leaf.");
    }
}
