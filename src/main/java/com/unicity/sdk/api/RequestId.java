package com.unicity.sdk.api;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Request ID for tracking transactions
 */
public class RequestId implements ISerializable {
    private final byte[] publicKey;
    private final DataHash stateHash;
    private final byte[] hashValue;

    private RequestId(byte[] publicKey, DataHash stateHash, byte[] hashValue) {
        this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
        this.stateHash = stateHash;
        this.hashValue = Arrays.copyOf(hashValue, hashValue.length);
    }

    public static CompletableFuture<RequestId> create(byte[] publicKey, DataHash stateHash) {
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(publicKey);
        hasher.update(stateHash.getImprint());
        
        return hasher.digest().thenApply(hash -> {
            byte[] hashBytes = hash.getHash();
            return new RequestId(publicKey, stateHash, hashBytes);
        });
    }
    
    /**
     * Creates a RequestId from an ID and hash imprint.
     * This matches TypeScript's RequestId.createFromImprint method.
     */
    public static CompletableFuture<RequestId> createFromImprint(byte[] id, byte[] hashImprint) {
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(id);
        hasher.update(hashImprint);
        
        return hasher.digest().thenApply(hash -> {
            // For createFromImprint, the resulting hash IS the state hash
            // We use the id as publicKey for compatibility
            return new RequestId(id, hash, hash.getHash());
        });
    }

    public byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }

    public DataHash getStateHash() {
        return stateHash;
    }
    
    /**
     * Get the hash of this RequestId.
     * This is the computed hash value, not the state hash.
     */
    public DataHash getHash() {
        return new DataHash(hashValue, HashAlgorithm.SHA256);
    }

    public BigInteger toBigInt() {
        // TypeScript SDK prepends "0x01" to the hash before converting to BigInt
        String hexString = "01" + HexConverter.encode(hashValue);
        return new BigInteger(hexString, 16);
    }
    
    /**
     * Create RequestId from JSON representation (hex string of the hash imprint).
     */
    public static RequestId fromJSON(String hashImprintHex) {
        DataHash hash = DataHash.fromJSON(hashImprintHex);
        // TODO Create a dummy RequestId with the hash
        // Note: We don't have the original publicKey and stateHash from just the hash
        return new RequestId(new byte[0], hash, hash.getHash());
    }

    @Override
    public Object toJSON() {
        // Return the hash imprint (algorithm prefix + hash) to match TypeScript implementation
        return getHash().toJSON();
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            CborEncoder.encodeByteString(publicKey),
            stateHash.toCBOR()
        );
    }
}