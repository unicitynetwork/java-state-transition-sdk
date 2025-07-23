
package com.unicity.sdk.token;

import com.unicity.sdk.util.BitString;
import com.unicity.sdk.util.HexConverter;
import java.util.Arrays;

/**
 * Globally unique identifier of a token.
 */
public class TokenId {
    private final byte[] bytes;

    public TokenId(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    public BitString toBitString() {
        return new BitString(this.bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenId tokenId = (TokenId) o;
        return Arrays.equals(this.bytes, tokenId.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return String.format("TokenId[%s]", HexConverter.encode(this.bytes));
    }
}
