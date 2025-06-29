package com.unicity.sdk.api;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

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

    public byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }

    public DataHash getStateHash() {
        return stateHash;
    }

    public BigInteger toBigInt() {
        return new BigInteger(1, hashValue);
    }

    @Override
    public Object toJSON() {
        return com.unicity.sdk.shared.util.HexConverter.encode(hashValue);
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            CborEncoder.encodeByteString(publicKey),
            stateHash.toCBOR()
        );
    }
}