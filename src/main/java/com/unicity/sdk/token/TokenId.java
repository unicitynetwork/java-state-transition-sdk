
package com.unicity.sdk.token;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.util.HexConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Globally unique identifier of a token.
 */
public class TokenId implements ISerializable {
    private final byte[] bytes;

    public TokenId(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public static TokenId create(byte[] id) {
        return new TokenId(id);
    }

    @Override
    @JsonValue
    public String toJSON() {
        return HexConverter.encode(this.bytes);
    }

    @Override
    public byte[] toCBOR() {
        CBORFactory factory = new CBORFactory();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CBORGenerator generator = factory.createGenerator(baos)) {
            generator.writeBinary(this.bytes);
            generator.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "TokenId[" + HexConverter.encode(this.bytes) + "]";
    }

    public BigInteger toBigInt() {
        return new BigInteger(1, toCBOR());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenId tokenId = (TokenId) o;
        return Arrays.equals(bytes, tokenId.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
