package com.unicity.sdk.api;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.BitString;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a unique request identifier derived from a public key and state hash.
 */
public class RequestId {
    private final DataHash hash;

    /**
     * Constructs a RequestId instance.
     * @param hash The DataHash representing the request ID.
     */
    private RequestId(DataHash hash) {
        this.hash = hash;
    }

    /**
     * Creates a RequestId from a public key and state hash.
     * @param id The public key as a byte array.
     * @param stateHash The state hash.
     * @return A CompletableFuture resolving to a RequestId instance.
     */
    public static CompletableFuture<RequestId> create(byte[] id, DataHash stateHash) {
        return createFromImprint(id, stateHash.getImprint());
    }
    
    /**
     * Creates a RequestId from a public key and hash imprint.
     * @param id The public key as a byte array.
     * @param hashImprint The hash imprint as a byte array.
     * @return A CompletableFuture resolving to a RequestId instance.
     */
    public static CompletableFuture<RequestId> createFromImprint(byte[] id, byte[] hashImprint) {
        DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
        hasher.update(id);
        hasher.update(hashImprint);
        
        return hasher.digest().thenApply(RequestId::new);
    }

    /**
     * Decodes a RequestId from CBOR bytes.
     * @param data The CBOR-encoded bytes.
     * @return A RequestId instance.
     */
    public static RequestId fromCBOR(byte[] data) {
        return new RequestId(DataHash.fromCBOR(data));
    }

    /**
     * Creates a RequestId from a JSON string.
     * @param data The JSON string.
     * @return A RequestId instance.
     */
    public static RequestId fromJSON(String data) {
        return new RequestId(DataHash.fromJSON(data));
    }

    /**
     * Converts the RequestId to a BitString.
     * @return The BitString representation of the RequestId.
     */
    public BitString toBitString() {
        return BitString.fromDataHash(hash);
    }

    /**
     * Gets the underlying DataHash.
     * @return The DataHash.
     */
    public DataHash getHash() {
        return hash;
    }

    /**
     * Encodes the RequestId to CBOR format.
     * @return The CBOR-encoded bytes.
     */
    public byte[] toCBOR() {
        return hash.toCBOR();
    }

    /**
     * Checks if this RequestId is equal to another.
     * @param obj The object to compare.
     * @return True if equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RequestId requestId = (RequestId) obj;
        return hash.equals(requestId.hash);
    }

    /**
     * Returns a string representation of the RequestId.
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "RequestId[" + hash.toString() + "]";
    }
}