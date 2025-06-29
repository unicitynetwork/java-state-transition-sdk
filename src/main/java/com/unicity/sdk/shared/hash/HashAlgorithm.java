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
    
    /**
     * Get HashAlgorithm from its numeric value.
     * @param value The numeric value
     * @return The corresponding HashAlgorithm
     * @throws IllegalArgumentException if value is not valid
     */
    public static HashAlgorithm fromValue(int value) {
        for (HashAlgorithm algorithm : HashAlgorithm.values()) {
            if (algorithm.getValue() == value) {
                return algorithm;
            }
        }
        throw new IllegalArgumentException("Invalid HashAlgorithm value: " + value);
    }
}