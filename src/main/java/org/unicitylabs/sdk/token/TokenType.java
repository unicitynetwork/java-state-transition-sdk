
package org.unicitylabs.sdk.token;

import org.unicitylabs.sdk.util.HexConverter;
import java.util.Arrays;

/**
 * Unique identifier describing the type/category of a token.
 */
public class TokenType {
    private final byte[] bytes;

    public TokenType(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }
        TokenType tokenType = (TokenType) o;
        return Arrays.equals(this.bytes, tokenType.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }

    @Override
    public String toString() {
        return String.format("TokenType[%s]", HexConverter.encode(this.bytes));
    }
}
