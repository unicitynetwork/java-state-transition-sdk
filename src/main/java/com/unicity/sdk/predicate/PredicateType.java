
package com.unicity.sdk.predicate;

public enum PredicateType {
    UNMASKED("UNMASKED"),
    MASKED("MASKED"),
    BURN("BURN");
    
    private final String type;
    
    PredicateType(String value) {
        this.type = value;
    }
    
    public String getType() {
        return this.type;
    }
}
