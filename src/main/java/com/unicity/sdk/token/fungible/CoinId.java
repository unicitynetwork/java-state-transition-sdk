
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.util.HexConverter;
import java.util.Arrays;

public class CoinId {
    private final byte[] bytes;

    public CoinId(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoinId coinId = (CoinId) o;
        return Arrays.equals(this.bytes, coinId.bytes);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return String.format("CoinId{bytes=%s}", HexConverter.encode(this.bytes));
    }
}
