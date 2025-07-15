package com.unicity.sdk.address;

import com.fasterxml.jackson.annotation.JsonValue;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Direct address implementation
 */
public class DirectAddress implements IAddress {
    private static final int ADDRESS_LENGTH = 36;
    private static final byte ADDRESS_TYPE = 0x00;
    
    private final byte[] bytes;

    private DirectAddress(byte[] bytes) {
        if (bytes.length != ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Direct address must be " + ADDRESS_LENGTH + " bytes");
        }
        if (bytes[0] != ADDRESS_TYPE) {
            throw new IllegalArgumentException("Invalid address type for DirectAddress");
        }
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public static CompletableFuture<DirectAddress> create(DataHash reference) {
        byte[] addressBytes = new byte[ADDRESS_LENGTH];
        addressBytes[0] = ADDRESS_TYPE;
        System.arraycopy(reference.getData(), 0, addressBytes, 1, Math.min(reference.getData().length, ADDRESS_LENGTH - 1));
        return CompletableFuture.completedFuture(new DirectAddress(addressBytes));
    }

    public static CompletableFuture<DirectAddress> fromJSON(String json) {
        return CompletableFuture.completedFuture(new DirectAddress(HexConverter.decode(json)));
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    @Override
    public String getAddress() {
        return HexConverter.encode(bytes);
    }

    @Override
    @JsonValue
    public Object toJSON() {
        return getAddress();
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeByteString(bytes);
    }

    @Override
    public String toString() {
        return getAddress();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectAddress that = (DirectAddress) o;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}