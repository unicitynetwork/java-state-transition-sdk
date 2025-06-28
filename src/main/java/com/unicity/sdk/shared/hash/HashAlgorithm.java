package com.unicity.sdk.shared.hash;

public enum HashAlgorithm {
    SHA256(0),
    SHA224(1),
    SHA384(2),
    SHA512(3),
    RIPEMD160(4);

    private final int value;

    HashAlgorithm(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}