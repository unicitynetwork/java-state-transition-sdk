package com.unicity.sdk.shared.smt;

public class LeafOutOfBoundsException extends Exception {
    public LeafOutOfBoundsException() {
        super("Cannot extend tree through leaf.");
    }
}
