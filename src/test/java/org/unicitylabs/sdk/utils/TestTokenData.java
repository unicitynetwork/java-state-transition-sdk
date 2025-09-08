package org.unicitylabs.sdk.utils;

import org.unicitylabs.sdk.util.HexConverter;

import java.util.Arrays;

/**
 * Test token data implementation
 */
public class TestTokenData {
    private final byte[] data;

    public TestTokenData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    @Override
    public String toString() {
        return "TestTokenData: " + HexConverter.encode(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestTokenData that = (TestTokenData) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}
