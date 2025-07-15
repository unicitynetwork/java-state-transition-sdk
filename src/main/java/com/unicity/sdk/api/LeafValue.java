package com.unicity.sdk.api;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.util.concurrent.CompletableFuture;

/**
 * Leaf value for merkle tree
 */
public class LeafValue implements ISerializable {
    private final byte[] bytes;

    private LeafValue(byte[] bytes) {
        this.bytes = bytes;
    }

    public static CompletableFuture<LeafValue> create(Authenticator authenticator, DataHash transactionHash) {
        DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
        hasher.update(authenticator.toCBOR());
        hasher.update(transactionHash.getImprint());
        
        return hasher.digest().thenApply(hash -> new LeafValue(hash.getImprint()));
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        LeafValue leafValue = (LeafValue) other;
        return java.util.Arrays.equals(bytes, leafValue.bytes);
    }

    @Override
    public Object toJSON() {
        return com.unicity.sdk.shared.util.HexConverter.encode(bytes);
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeByteString(bytes);
    }
}