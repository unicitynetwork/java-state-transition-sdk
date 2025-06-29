
package com.unicity.sdk.token;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.util.HexConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Unique identifier describing the type/category of a token.
 */
public class TokenType implements ISerializable {
    private final byte[] bytes;

    public TokenType(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public static TokenType create(byte[] id) {
        return new TokenType(id);
    }

    @Override
    @JsonValue
    public String toJSON() {
        return HexConverter.encode(this.bytes);
    }

    @Override
    public byte[] toCBOR() {
        return com.unicity.sdk.shared.cbor.CborEncoder.encodeByteString(bytes);
    }

    @Override
    public String toString() {
        return "TokenType[" + HexConverter.encode(this.bytes) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenType tokenType = (TokenType) o;
        return Arrays.equals(bytes, tokenType.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
