package com.unicity.sdk.smt;

public class LeafOutOfBoundsException extends Exception {
    public LeafOutOfBoundsException() {
        super("Cannot extend tree through leaf.");
    }
}
