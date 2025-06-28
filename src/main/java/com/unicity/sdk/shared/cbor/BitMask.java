
package com.unicity.sdk.shared.cbor;

public enum BitMask {
    MAJOR_TYPE(0b11100000),
    ADDITIONAL_INFO(0b00011111);

    private final int mask;

    BitMask(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }
}
