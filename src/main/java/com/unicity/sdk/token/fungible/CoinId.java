
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.util.HexConverter;

import java.util.Arrays;

public class CoinId implements ISerializable {
    private final byte[] bytes;

    public CoinId(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public Object toJSON() {
        return HexConverter.encode(bytes);
    }

    @Override
    public byte[] toCBOR() {
        return bytes;
    }
}
