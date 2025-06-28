
package com.unicity.sdk.address;

public enum AddressScheme {
    DIRECT("DIRECT");

    private final String scheme;

    AddressScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getScheme() {
        return scheme;
    }
}
