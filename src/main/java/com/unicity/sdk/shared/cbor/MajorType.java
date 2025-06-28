
package com.unicity.sdk.shared.cbor;

public enum MajorType {
    UNSIGNED_INTEGER(0x00),
    NEGATIVE_INTEGER(0x20),
    BYTE_STRING(0x40),
    TEXT_STRING(0x60),
    ARRAY(0x80),
    MAP(0xA0),
    TAG(0xC0),
    SIMPLE_OR_FLOAT(0xE0);
    
    private final int value;
    
    MajorType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
