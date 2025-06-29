
package com.unicity.sdk.predicate;

public enum PredicateType {
    UNMASKED(0),
    MASKED(1),
    BURN(2),
    DEFAULT(3);
    
    private final int value;
    
    PredicateType(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
