package com.unicity.sdk.mtree;

public class LeafOutOfBoundsException extends Exception {
    public LeafOutOfBoundsException() {
        super("Cannot extend tree through leaf.");
    }
}
