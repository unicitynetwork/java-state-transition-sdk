package com.unicity.sdk.api;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.util.concurrent.CompletableFuture;

/**
 * Leaf value for merkle tree
 */
public class LeafValue implements ISerializable {
    private final DataHash hash;

    private LeafValue(DataHash hash) {
        this.hash = hash;
    }

    public static CompletableFuture<LeafValue> create(Authenticator authenticator, DataHash transactionHash) {
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(authenticator.toCBOR());
        hasher.update(transactionHash.toCBOR());
        
        return hasher.digest().thenApply(LeafValue::new);
    }

    public DataHash getHash() {
        return hash;
    }

    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        LeafValue leafValue = (LeafValue) other;
        return hash.equals(leafValue.hash);
    }

    @Override
    public Object toJSON() {
        return hash.toJSON();
    }

    @Override
    public byte[] toCBOR() {
        return hash.toCBOR();
    }
}