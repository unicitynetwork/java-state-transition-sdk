
package com.unicity.sdk.shared.hash;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.util.HexConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DataHash implements ISerializable {
    private final byte[] hash;
    private final HashAlgorithm algorithm;

    public DataHash(byte[] hash, HashAlgorithm algorithm) {
        this.hash = Arrays.copyOf(hash, hash.length);
        this.algorithm = algorithm;
    }

    public DataHash(byte[] cbor) {
        this.algorithm = HashAlgorithm.values()[cbor[0] & 0xFF];
        this.hash = Arrays.copyOfRange(cbor, 1, cbor.length);
    }

    public byte[] getHash() {
        return Arrays.copyOf(hash, hash.length);
    }

    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public Object toJSON() {
        return HexConverter.encode(toCBOR());
    }

    @Override
    public byte[] toCBOR() {
        byte[] bytes = new byte[hash.length + 1];
        bytes[0] = (byte) algorithm.getValue();
        System.arraycopy(hash, 0, bytes, 1, hash.length);
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataHash dataHash = (DataHash) o;
        return Arrays.equals(hash, dataHash.hash) && algorithm == dataHash.algorithm;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(hash);
        result = 31 * result + (algorithm != null ? algorithm.hashCode() : 0);
        return result;
    }
}
