
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.util.HexConverter;

import java.math.BigInteger;
import java.util.Arrays;

public class CoinId implements ISerializable {
    private final byte[] bytes;
    private final BigInteger value;

    public CoinId(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.value = new BigInteger(1, bytes); // Unsigned BigInteger
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    public BigInteger getValue() {
        return value;
    }

    @Override
    public Object toJSON() {
        return HexConverter.encode(bytes);
    }

    @Override
    public byte[] toCBOR() {
        // Encode as byte string representing the BigInteger value
        return CborEncoder.encodeByteString(value.toByteArray());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoinId coinId = (CoinId) o;
        return Arrays.equals(bytes, coinId.bytes);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
